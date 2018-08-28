package cm.pvp.voyagepvp.voyagecore.features.cmdblocker;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CMDBlocker extends Feature implements Listener
{
    @ConfigPopulate("features.cmdblocker.blockedcommands")
    private List<String> blockedCommands = Lists.newArrayList();

    @ConfigPopulate("features.cmdblocker.messages.commandblocked")
    private String commandBlockedMessage;

    public CMDBlocker(VoyageCore instance)
    {
        super(instance, "CMDBlocker", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().getMainConfig().populate(this);
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(PlayerCommandPreprocessEvent e)
    {
        if (e.getMessage().split(" ")[0].split(":").length != 1) {
            e.setCancelled(true);
            return;
        }

        if (blockedCommands.stream().anyMatch(cmd -> e.getMessage().startsWith(cmd))) {
            e.setCancelled(true);
        }
    }
}
