package cm.pvp.voyagepvp.voyagecore.features.norain;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class NoRain extends Feature implements Listener
{
    public NoRain(VoyageCore instance)
    {
        super(instance, "NoRain", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(WeatherChangeEvent e)
    {
        e.setCancelled(true);
    }
}
