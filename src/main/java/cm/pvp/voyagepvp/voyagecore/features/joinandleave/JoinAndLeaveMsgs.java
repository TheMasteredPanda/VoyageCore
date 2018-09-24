package cm.pvp.voyagepvp.voyagecore.features.joinandleave;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndLeaveMsgs extends Feature implements Listener
{
    @ConfigPopulate("features.joinandleavemsgs.format.join")
    private String joinFormat;

    @ConfigPopulate("features.joinandleavemsgs.format.leave")
    private String leaveFormat;

    @ConfigPopulate("features.joinandleavemsgs.enabled")
    private boolean enabled;

    public JoinAndLeaveMsgs()
    {
        super("Join&LeaveMsgs", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().getMainConfig().populate(this);
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        if (!enabled) {
            e.setJoinMessage(null);
            return;
        }

        e.setJoinMessage(Format.format(joinFormat, "{playername};" + e.getPlayer().getName()));
    }

    @EventHandler
    public void on(PlayerQuitEvent e)
    {
        if (!enabled) {
            e.setQuitMessage(null);
            return;
        }

        e.setQuitMessage(Format.format(leaveFormat, "{playername};" + e.getPlayer().getName()));
    }
}
