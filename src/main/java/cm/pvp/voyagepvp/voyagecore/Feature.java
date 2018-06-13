package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.config.ConfigManager;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
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

    }

    @Override
    protected void boot()
    {
        getLogger().info("Enabling module.");

        Config config = getInstance().get(ConfigManager.class).get("mainConfig");

        if (!isEnabled() && config.raw().getBoolean("features." + getName().toLowerCase() + ".enabled")) {
            section = config.raw().getConfigurationSection("features." + getName().toLowerCase());

            try {
                setEnabled(enable());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isEnabled()) {
                getLogger().info("Enabled module.");
            } else {
                getLogger().warning("Couldn't enable module.");
            }
        }
    }
}
