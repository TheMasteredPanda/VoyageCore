package cm.pvp.voyagepvp.voyagecore.features.mention;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mention extends Feature implements Listener
{
    private Pattern mentionPattern = Pattern.compile("(@)\\w+");

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

        System.out.println("Invoked chat event.");

        for (String word : splitMessage) {
            Matcher matcher = mentionPattern.matcher(e.getMessage());


            if (!matcher.matches()) {
                continue;
            }

            System.out.println("Matcher success.");
            String[] msg = e.getMessage().split("(@)\\w+");
            String word1 = Arrays.stream(e.getMessage().split(" ")).filter(w -> mentionPattern.matcher(w).matches()).findFirst().orElse(null);

            if (word1 == null) {
                throw new UnsupportedOperationException("Found a mention, but couldn't get the mention.");
            }

            word1 = word1.replace("@", "");

            TextComponent message = new TextComponent(msg[0]);

            Optional<PlayerProfile> profile = getInstance().getMojangLookup().lookup(word1);

            if (!profile.isPresent()) {
                e.getPlayer().sendMessage(Format.colour(Format.format(playerNotFound, "{player};" + word1)));
                e.setCancelled(true);
            }

            PlayerProfile p = profile.get();
            TextComponent mentioned = new TextComponent(ChatColor.YELLOW + "@" + word1 + ChatColor.RESET);
            List<String> clone = Lists.newArrayList(infoTemplate);
            clone.replaceAll(s -> s.replace("{online}", String.valueOf(Bukkit.getPlayer(p.getId()).isOnline())).replace("{uuid}", p.getId().toString()));
            mentioned.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Joiner.on('\n').join(clone))));
            message.addExtra(mentioned);
            if (msg.length != 1) message.addExtra(new TextComponent(msg[1]));
        }

    }
}
