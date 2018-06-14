package cm.pvp.voyagepvp.voyagecore.api.module;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import com.google.common.collect.ArrayListMultimap;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manager to manage modules.
 */
public final class ModuleManager extends Manager<VoyageCore>
{
    private ArrayListMultimap<JavaPlugin, Module> modules = ArrayListMultimap.create();

    public ModuleManager(VoyageCore instance)
    {
        super(instance, "Module Manager", 1.00);
    }

    /**
     * Add modules to the manager.
     * @param instance - instance of the plugin the modules belong to.
     * @param modules - the modules.
     */
    public void add(JavaPlugin instance, Module... modules)
    {
        for (Module module : modules) {
            if (this.modules.get(instance).stream().anyMatch(m -> m.getName().equals(module.getName()))) {
                continue;
            }

            getLogger().info("Added module " + module.getName() + ".");
            this.modules.put(instance, module);
        }
    }


    @Override
    public boolean enable() throws Exception
    {
        modules.values().forEach(Module::boot);
        return true;
    }

    /**
     * Boot all modules belonging to the plugin.
     * @param instance - the instance of the plugin the modules belong to.
     */
    public void boot(JavaPlugin instance)
    {
        modules.get(instance).forEach(Module::boot);
    }

    /**
     * Shuts down all modules loaded.
     */
    public void shutdownModules()
    {
        modules.values().forEach(Module::shutdown);
    }
}
