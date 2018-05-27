package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;

public class VoyageCore extends VoyagePlugin
{
    private static VoyageCore instance;
    private ModuleManager moduleManager;

    public VoyageCore()
    {
        instance = this;
    }

    public ModuleManager getModuleManager()
    {
        if (moduleManager == null) {
            return new ModuleManager(this);
        }

        return moduleManager;
    }

    public static VoyageCore get()
    {
        return instance;
    }
}
