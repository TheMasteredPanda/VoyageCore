package cm.pvp.voyagepvp.voyagecore.features.mention;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
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

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Mention extends Feature implements Listener
{
    private Pattern behindCheck = Pattern.compile("(\\w+) (@)\\w+");
    private Pattern afterCheck = Pattern.compile("(@)\\w+ (\\w+)");

    @ConfigPopulate("features.mention.messages.playernotfound")
    private String playerNotFound;

    @ConfigPopulate("features.mention.messages.infotemplate")
    private List<String> infoTemplate;

    public Mention(VoyageCore instance)
    {
        super(instance, "Mention", 1.0);

        try {
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
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

        String[] splitMessage = e.getMessage().split(" ");
        LinkedList<String> names = Arrays.stream(splitMessage).filter(w -> w.matches("(@)\\w+")).collect(Collectors.toCollection(Lists::newLinkedList));
        TextComponent message = new TextComponent();


        for (Iterator<String> iterator = names.iterator(); iterator.hasNext(); ) {
            String name = iterator.next();

            for (String splitMsg : splitMessage) {
                if (splitMsg.contains(name)) {
                    continue;
                }

                String[] innerSplit = splitMsg.split(name);

                if (splitMsg.matches(behindCheck.pattern())) {
                    message.addExtra(Format.colour(innerSplit[0]));
                }

                Optional<PlayerProfile> optional = getInstance().getMojangLookup().lookup(name.replace("@", ""));

                if (!optional.isPresent()) {
                    e.getPlayer().sendMessage(Format.format(playerNotFound, "{player};" + name.replace("@", "")));
                    return;
                }

                PlayerProfile profile = optional.get();

                TextComponent mentioned = new TextComponent(name);
                mentioned.setColor(ChatColor.YELLOW);
                BaseComponent[] hover = TextComponent.fromLegacyText(Format.format(Joiner.on('\n').join(infoTemplate), "{online};" + String.valueOf(Bukkit.getPlayer(profile.getId()).isOnline()), "{uuid};" + profile.getId().toString()));
                mentioned.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
                message.addExtra(mentioned);

                if (splitMsg.matches(afterCheck.pattern())) {
                    mentioned.addExtra(innerSplit[1]);
                }

                iterator.remove();
            }
        }
    }
}
