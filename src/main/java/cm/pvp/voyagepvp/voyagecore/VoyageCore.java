package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;

public class VoyageCore extends VoyagePlugin
{
    private static VoyageCore instance;
    private ModuleManager moduleManager;
    private MojangLookup mojangLookup;

    public VoyageCore()
    {
        instance = this;
    }

    public ModuleManager getModuleManager()
    {
        if (moduleManager == null) {
            moduleManager = new ModuleManager(this);
        }

        return moduleManager;
    }

    public static VoyageCore get()
    {
        return instance;
    }

    private MojangLookup getMojangLookup()
    {
        if (mojangLookup == null) {
            mojangLookup = new MojangLookup();
        }

        return mojangLookup;
    }
}
