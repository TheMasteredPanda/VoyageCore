package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.module.Module;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class Feature extends Module<VoyageCore>
{
    private VoyageCore instance = VoyageCore.get();
    private ConfigurationSection section;

    public Feature(String name, double version)
    {
        super(VoyageCore.get(), name, version);
        section = instance.getMainConfig().raw().getConfigurationSection("features." + name.toLowerCase());
    }
}
