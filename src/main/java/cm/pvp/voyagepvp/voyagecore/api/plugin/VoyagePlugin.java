package cm.pvp.voyagepvp.voyagecore.api.plugin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;
import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import cm.pvp.voyagepvp.voyagecore.api.reflect.ReflectUtil;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.MethodAccessor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * Main class of Voyage plugins.
 */
public class VoyagePlugin extends JavaPlugin
{
    private HashMap<Class, Manager> managers = Maps.newHashMap();

    /**
     * Add an array of managers owned by this plugin.
     * @param managers - managers.
     */
    public void add(Manager... managers)
    {
        for (int i = 0; i < managers.length; i++) {
            Manager manager = managers[i];

            if (this.managers.values().stream().anyMatch(m -> m.getName().equals(manager.getName()))) {
                continue;
            }

            getLogger().info("Added manager " + manager.getName() + ".");
            this.managers.put(manager.getClass(), manager);
        }
    }

    /**
     * Boot all managers.
     */
    public void bootAll()
    {
        managers.values().forEach(Manager::boot);
    }

    /**
     * Shutdown all managers.
     */
    public void shutdownAll()
    {
        managers.values().forEach(Manager::shutdown);
    }

    /**
     * Get a manager instance.
     * @param manager - manager type.
     * @param <T> - generic type.
     * @return manager instance.
     */
    public <T extends Manager> T get(Class<T> manager)
    {
        return GenericUtil.cast(managers.get(manager));
    }

    /**
     * Register voyage command wrappers.
     * @param commands - immutable array of commands to register.
     */
    public void register(VoyageCommand... commands)
    {
        MethodAccessor<SimpleCommandMap> getCommandMap = ReflectUtil.getMethod(ReflectUtil.getOBCClass("CraftServer"), "getCommandMap", true);
        SimpleCommandMap map = getCommandMap.invoke(getServer());
        Preconditions.checkNotNull(map, "Command map is null.");

        for (VoyageCommand cmd : commands) {
            map.register(getDescription().getName().toLowerCase(), cmd);
            getLogger().info("Registered command " + cmd.getName() + ".");
        }
    }
}
