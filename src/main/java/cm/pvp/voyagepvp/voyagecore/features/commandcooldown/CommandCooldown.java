package cm.pvp.voyagepvp.voyagecore.features.commandcooldown;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CommandCooldown extends Feature implements Listener
{
    private HashMap<String, Integer> commands = Maps.newHashMap();
    private ArrayListMultimap<UUID, CooldownEntry> entries = ArrayListMultimap.create();


    @ConfigPopulate("features.commandcooldown.messages.timeleft")
    private String timeLeftMessage;

    @ConfigPopulate("features.commandcooldown.messages.cooldownadded")
    private String cooldownAddedMessage;

    public CommandCooldown()
    {
        super("CommandCooldown", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        for (Map.Entry<String, Object> entry : getSection().getConfigurationSection("commands").getValues(true).entrySet()) {
            String command = entry.getKey().replace("-", " ");

            if (!NumberUtil.parseable(entry.getValue().toString(), int.class)) {
                getLogger().info("Cooldown for command '" + command + "' is not a number.");
                return false;
            }

            int cooldown = NumberUtil.parse(entry.getValue().toString(), int.class);
            commands.put(command, cooldown);
            getLogger().info("Added command '" + command + "' with the cooldown of " + String.valueOf(cooldown) + " seconds.");
        }


        Tasks.runAsyncRepeating(() -> {
            Iterator<Map.Entry<UUID, CooldownEntry>> iterator = entries.entries().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, CooldownEntry> entry = iterator.next();

                entry.getValue().setLeft(entry.getValue().getLeft() - 1);

                if (entry.getValue().getLeft() <= 0) {
                    iterator.remove();
                }
            }
        }, 20L);


        getInstance().getMainConfig().populate(this);

        return true;
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e)
    {
        Map.Entry<String, Integer> command = commands.entrySet().stream().filter(entry -> entry.getKey().contains(e.getMessage())).findFirst().orElse(null);

        if (command == null) {
            return;
        }

        if (entries.containsKey(e.getPlayer().getUniqueId())) {
            int cooldown = entries.get(e.getPlayer().getUniqueId()).stream().filter(entry -> entry.getCommand().equalsIgnoreCase(command.getKey())).findFirst().map(CooldownEntry::getLeft).orElseGet(() -> -1);

            e.getPlayer().sendMessage(Format.colour(Format.format(timeLeftMessage, "{time};" + String.valueOf(cooldown))));
            e.setCancelled(true);
        }

        entries.put(e.getPlayer().getUniqueId(), new CooldownEntry(command.getKey(), command.getValue()));
        e.getPlayer().sendMessage(Format.colour(Format.format(cooldownAddedMessage, "{time};" + String.valueOf(command.getValue()))));
    }

    @Getter
    @AllArgsConstructor
    public class CooldownEntry
    {
        private String command;

        @Setter
        private int left;
    }
}
