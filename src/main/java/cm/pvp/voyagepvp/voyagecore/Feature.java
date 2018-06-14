package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.module.Module;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

public class Feature extends Module<VoyageCore>
{
    @Getter
    private ConfigurationSection section;

    public Feature(VoyageCore instance, String name, double version)
    {
        super(instance, name, version);
        section = instance.getMainConfig().raw().getConfigurationSection("features.announcements");
    }
}
