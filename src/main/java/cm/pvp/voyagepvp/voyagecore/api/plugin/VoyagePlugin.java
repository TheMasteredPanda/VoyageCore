package cm.pvp.voyagepvp.voyagecore.api.plugin;

import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Main class of Voyage plugins.
 */
public class VoyagePlugin extends JavaPlugin
{
    private ArrayList<Manager> managers = Lists.newArrayList();

    /**
     * Add an array of managers owned by this plugin.
     * @param managers - managers.
     */
    public void add(final Manager... managers)
    {
        for (int i = 0; i < managers.length; i++) {
            Manager manager = managers[i];

            if (this.managers.stream().anyMatch(m -> m.getName().equals(manager.getName()))) {
                continue;
            }

            this.managers.add(manager);
        }
    }

    /**
     * Boot all managers.
     */
    public void bootAll()
    {
        managers.forEach(Manager::boot);
    }

    /**
     * Shutdown all managers.
     */
    public void shutdownAll()
    {
        managers.forEach(Manager::shutdown);
    }



}
