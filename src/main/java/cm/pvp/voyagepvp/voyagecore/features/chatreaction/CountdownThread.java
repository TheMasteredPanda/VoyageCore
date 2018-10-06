package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.TimeUtil;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import de.Herbystar.TTA.TTA_Methods;
import javafx.util.Pair;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class CountdownThread extends BukkitRunnable implements Listener
{
    private VoyageCore instance;
    private ChatReaction feature;
    private int countdown;
    private long started;
    private Pair<String, String> word;

    @ConfigPopulate("features.chatreaction.messages.countdown")
    private String countdownMessage;

    @ConfigPopulate("features.chatreaction.messages.wordunscrambled")
    private String wordUnscambled;

    @ConfigPopulate("features.chatreaction.messages.wordnotunscrambled")
    private String wordNotUnscrambled;

    @ConfigPopulate("features.chatreaction.messages.broadcast")
    private String broadcast;

    public CountdownThread(VoyageCore instance, ChatReaction feature, Pair<String, String> word)
    {
        this.instance = instance;
        this.feature = feature;
        this.word = word;
        instance.getMainConfig().populate(this);
        countdown = feature.getSection().getInt("countdown");
        started = System.currentTimeMillis();
        if (feature.getScambleThread() != null) feature.getScambleThread().cancel();
        feature.setScambleThread(null);
        TextComponent component = new TextComponent(Format.colour(broadcast.split("\\{randomword}")[0].replace("{time}", TimeUtil.millisecondsToTimeUnits(TimeUnit.SECONDS, countdown, true))));
        TextComponent randomWord = new TextComponent(word.getValue());
        randomWord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(word.getValue())));
        component.addExtra(randomWord);
        component.addExtra(Format.colour(broadcast.split("\\{randomword}")[1].replace("{time}", TimeUtil.millisecondsToTimeUnits(TimeUnit.SECONDS, countdown, true))));
        Bukkit.spigot().broadcast(component);
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
         runTaskTimerAsynchronously(instance, 0L, 20);
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent e)
    {
        if (e.getMessage().split(" ").length != 1) {
            return;
        }

        if (e.getMessage().equals(word.getKey())) {
            long time = System.currentTimeMillis() - started;
            Bukkit.broadcastMessage(Format.colour(Format.format(wordUnscambled, "{winner};" + e.getPlayer().getName(), "{time};" + TimeUtil.millisecondsToTimeUnits(TimeUnit.MILLISECONDS, time, true), "{word};" + word.getKey())));
            Tasks.runSyncLater(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), feature.getSection().getString("rewardcommand").replace("{player}", e.getPlayer().getName())), 0L);

            feature.getHandler().get(e.getPlayer().getUniqueId()).whenComplete((player, throwable) -> {
                if (player == null) throw new RuntimeException("Couldn't find ReactionPlayer instance.");

                player.setWins(player.getWins() + 1);
                if (player.getFastest() < time) player.setFastest(time);
                feature.getHandler().update(player);
            });

            HandlerList.unregisterAll(this);
            feature.setScambleThread(new ScrambleBroadcasterThread(instance, feature));
        }
    }

    @Override
    public void run()
    {
        countdown--;

        if (countdown <= 0) {
            Bukkit.broadcastMessage(Format.colour(Format.format(wordNotUnscrambled, "{word};" + word.getKey())));
            feature.setScambleThread(new ScrambleBroadcasterThread(instance, feature));
            HandlerList.unregisterAll(this);
        } else {
            if (countdown <= 10) {
                Bukkit.getOnlinePlayers().forEach(p -> TTA_Methods.sendActionBar(p, Format.colour(Format.format(countdownMessage, "{countdown};" + TimeUtil.millisecondsToTimeUnits(TimeUnit.MILLISECONDS, countdown, false)))));
            }
        }
    }
}
