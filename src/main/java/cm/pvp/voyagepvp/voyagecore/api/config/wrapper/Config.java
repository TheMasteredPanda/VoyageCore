package cm.pvp.voyagepvp.voyagecore.api.config.wrapper;

import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;

public class Config<T extends VoyagePlugin>
{
    private File configFile;
    private YamlConfiguration config;

    public Config(T instance, File config) throws FileNotFoundException
    {
        this.configFile = config;

        if (!config.getParentFile().exists()) {
            config.getParentFile().mkdirs();
        }

        if (!config.exists()) {
            instance.saveResource(instance.getName(), false);

            if (!config.exists()) {
                throw new FileNotFoundException("Couldn't find file " + config.getName() + " bundled with plugin " + instance.getDescription().getName() + ".");
            }
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public YamlConfiguration raw()
    {
        return config;
    }

    public void populate(Object instance)
    {

    }
}
