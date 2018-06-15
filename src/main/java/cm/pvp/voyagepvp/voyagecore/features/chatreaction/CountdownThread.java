package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.TimeUtil;
import de.Herbystar.TTA.TTA_Methods;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.naming.OperationNotSupportedException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CountdownThread extends BukkitRunnable implements Listener
{
    private VoyageCore instance;
    private ChatReaction feature;
    private int countdown = 0;
    private long started;
    private Pair<String, String> word;

    @ConfigPopulate("features.chatreaction.messages.Countdown")
    private String countdownMessage;

    @ConfigPopulate("features.chatreaction.messages.WordUnscrambled")
    private String wordUnscambled;

    @ConfigPopulate("features.chatreaction.messages.WordNotUnscrambled")
    private String wordNotUnscrambled;


    public CountdownThread(VoyageCore instance, ChatReaction feature, Pair<String, String> word)
    {
        this.instance = instance;
        this.feature = feature;
        this.word = word;

        try {
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }

        started = System.currentTimeMillis();
        if (feature.getScambleThread() != null) feature.getScambleThread().cancel();
        feature.setScambleThread(null);
        Bukkit.getServer().getPluginManager().registerEvents(this, instance);
        runTaskTimerAsynchronously(instance, 0L, feature.getSection().getInt("countdown") * 20);
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e)
    {
        if (e.getMessage().split(" ").length != 1) {
            return;
        }

        if (e.getMessage().equals(word.getKey())) {
            long time = System.currentTimeMillis() - started;
            Bukkit.broadcastMessage(Format.colour(Format.format(wordUnscambled, "{winner};" + e.getPlayer().getName(), "{time};" + TimeUtil.millisecondsToTimeUnits(TimeUnit.MINUTES, time, true))));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), feature.getSection().getString("RewardCommand").replace("{player}", e.getPlayer().getName()));
            Optional<ReactionPlayer> player = feature.getHandler().get(e.getPlayer().getUniqueId());

            if (!player.isPresent()) {
                throw new NumberFormatException("Couldn't find ReactionPlayer instance for " + e.getPlayer().getName());
            } else {
                player.get().setWins(player.get().getWins() + 1);
                if (player.get().getFastest() < time) player.get().setFastest(time);

                try {
                    feature.getHandler().update(player.get());
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }

            HandlerList.unregisterAll(this);
            feature.setScambleThread(new ScrambleBroadcasterThread(instance, feature));
        }
    }

    @Override
    public void run()
    {
        countdown--;

        if (countdown <= 0) {
            Bukkit.broadcastMessage(Format.colour(wordNotUnscrambled));
            feature.setScambleThread(new ScrambleBroadcasterThread(instance, feature));
            HandlerList.unregisterAll(this);
        } else {
            if (countdown <= 10) {
                Bukkit.getOnlinePlayers().forEach(p -> TTA_Methods.sendActionBar(p, Format.colour(Format.format(countdownMessage, "{countdown};" + TimeUtil.millisecondsToTimeUnits(TimeUnit.SECONDS, countdown, false)))));
            }
        }
    }
}
