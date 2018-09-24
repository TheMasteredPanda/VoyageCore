package cm.pvp.voyagepvp.voyagecore.features.antiportaltrap;

import cm.pvp.voyagepvp.voyagecore.Feature;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiPortalTrap extends Feature implements Listener
{
    public AntiPortalTrap()
    {
        super("AntiPortalTrap", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        Location location = e.getPlayer().getLocation();
        Material bottom = location.subtract(0D, 1D, 0D).getBlock().getType();
        Material top = location.add(0D, 1D, 0D).getBlock().getType();

        if (bottom.equals(Material.PORTAL) || bottom.equals(Material.ENDER_PORTAL) || bottom.equals(Material.ENDER_PORTAL_FRAME) || top.equals(Material.PORTAL) || top.equals(Material.ENDER_PORTAL) || top.equals(Material.ENDER_PORTAL_FRAME)) {
            Location spawnLocation = e.getPlayer().getWorld().getSpawnLocation();
            e.getPlayer().teleport(spawnLocation);
        }
    }
}
