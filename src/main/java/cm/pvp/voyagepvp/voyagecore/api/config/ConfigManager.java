package cm.pvp.voyagepvp.voyagecore.api.config;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import com.google.common.collect.Maps;

import java.util.HashMap;

public class ConfigManager<T extends VoyagePlugin> extends Manager<T>
{
    private HashMap<String, Config> wrappers = Maps.newHashMap();

    public ConfigManager(T instance)
    {
        super(instance, "Config Manager/" + instance.getDescription().getName(), 1.00);
    }


}
