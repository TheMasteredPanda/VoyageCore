package cm.pvp.voyagepvp.voyagecore.features.mention;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Mention extends Feature implements Listener
{
    private Pattern check = Pattern.compile("@([a-zA-Z0-9_]{3,16})");

    @ConfigPopulate("features.mention.messages.playernotfound")
    private String playerNotFound;

    @ConfigPopulate("features.mention.messages.infotemplate")
    private List<String> infoTemplate;

    public Mention()
    {
        super("Mention", 2.0);

        getInstance().getMainConfig().populate(this);
    }

    @Override
    protected boolean enable() throws Exception
    {
        Bukkit.getServer().getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent e)
    {
        String[] splitMessage = Format.format(e.getFormat(), "%1$s;" + e.getPlayer().getDisplayName(), "%2$s;" + e.getMessage()).split(" ");
        LinkedList<String> names = Arrays.stream(splitMessage).filter(w -> check.matcher(w).matches()).collect(Collectors.toCollection(Lists::newLinkedList));
        TextComponent message = new TextComponent();

        for (Iterator<String> iterator = names.iterator(); iterator.hasNext(); ) {
            String name = iterator.next();

            for (String splitMsg : splitMessage) {

                if (!splitMsg.contains(name)) {
                    message.addExtra(splitMsg);
                    message.addExtra(" ");
                    continue;
                }

                Optional<PlayerProfile> optional = getInstance().getLocalLookup().lookup(name.replace("@", ""));

                if (!optional.isPresent()) {
                    e.getPlayer().sendMessage(Format.format(playerNotFound, "{player};" + name.replace("@", "")));
                    message.addExtra(splitMsg);
                    message.addExtra(" ");
                    continue;
                }

                PlayerProfile profile = optional.get();
                TextComponent mentioned = new TextComponent(name);
                mentioned.setColor(ChatColor.YELLOW);
                BaseComponent[] hover = TextComponent.fromLegacyText(Format.colour(Format.format(Joiner.on('\n').join(infoTemplate), "{online};" + String.valueOf(Bukkit.getOfflinePlayer(profile.getId()).isOnline()), "{uuid};" + profile.getId().toString())));
                mentioned.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                message.addExtra(mentioned);
                mentioned.addExtra(" ");
                iterator.remove();
            }

            e.setCancelled(true);
            Bukkit.spigot().broadcast(message);
        }
    }
}
