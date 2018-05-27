package cm.pvp.voyagepvp.voyagecore.api.config;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
import cm.pvp.voyagepvp.voyagecore.api.exception.MapException;
import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import com.google.common.collect.Maps;

import java.util.HashMap;

/**
 * Manager for managing configuration wrappers.
 * @param <T> - plugin generic type.
 */
public class ConfigManager<T extends VoyagePlugin> extends Manager<T>
{
    private HashMap<String, Config> wrappers = Maps.newHashMap();

    public ConfigManager(T instance)
    {
        super(instance, "Config Manager/" + instance.getDescription().getName(), 1.00);
    }

    /**
     * Add a wrapper.
     * @param wrapper - wrapper to add.
     */
    public void add(Config wrapper)
    {
        if (wrappers.containsKey(wrapper.getConfigFile().getName())) {
            throw new MapException("The key " + wrapper.getConfigFile().getName() + " is already being used. Perhaps you've already added the wrapper?");
        }

        wrappers.put(wrapper.getConfigFile().getName(), wrapper);
    }

    /**
     * Remove a wrapper.
     * @param name - the file the wrapper is wrapping.
     */
    public void remove(String name)
    {
        wrappers.remove(name);
    }

    /**
     * Remove a wrapper.
     * @param wrapper - the wrapper instance to remove.
     */
    public void remove(Config wrapper)
    {
        remove(wrapper.getConfigFile().getName());
    }

    /**
     * Remove all wrappers.
     */
    public void removeAll()
    {
        wrappers.clear();
    }

    /**
     * Get a wrapper.
     * @param name - wrapper name.
     * @return if wrapper is found in the map then it will return the instance, otherwise it will throw a NullPointerException.
     */
    public Config get(String name)
    {
        if (!wrappers.containsKey(name)) {
            throw new NullPointerException("Couldn't find wrapper wrapping file named " + name + ".");
        }

        return wrappers.get(name);
    }
}
