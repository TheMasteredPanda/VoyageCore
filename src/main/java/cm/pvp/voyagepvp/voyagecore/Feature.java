package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.module.Module;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class Feature extends Module<VoyageCore>
{
    private VoyageCore instance;
    private ConfigurationSection section;

    public Feature(VoyageCore instance, String name, double version)
    {
        super(instance, name, version);
        this.instance = instance;
        section = instance.getMainConfig().raw().getConfigurationSection("features." + name.toLowerCase());
    }
}
