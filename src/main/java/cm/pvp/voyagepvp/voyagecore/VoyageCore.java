package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;

import java.io.File;
import java.io.FileNotFoundException;

public class VoyageCore extends VoyagePlugin
{
    private static VoyageCore instance;
    private ModuleManager moduleManager;
    private MojangLookup mojangLookup;
    private Config<VoyageCore> mainConfig;

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

    @Override
    public void onLoad()
    {
        try {
            mainConfig = new Config<>(this, new File(getDataFolder() + File.separator + "config.yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable()
    {
        bootAll();
    }
}
