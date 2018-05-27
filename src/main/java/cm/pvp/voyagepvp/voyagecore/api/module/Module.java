package cm.pvp.voyagepvp.voyagecore.api.module;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Template class for a module. Each module handles a feature.
 * @param <T>
 */
@Getter
public class Module<T extends JavaPlugin>
{
    /**
     * The logged name of the Module.
     */
    private String name;

    /**
     * The version of the plugin.
     */
    private double version;

    /**
     * For checking if the module is enabled or not.
     */
    private boolean enabled = false;

    /**
     * Logging instance for this module.
     */
    private Logger logger;

    /**
     * Instance of the plugin this module belongs to.
     */
    @Getter(value = AccessLevel.PROTECTED)
    private T instance;

    public Module(T instance, String name, double version)
    {
        this.instance = instance;
        this.name = name;
        this.version = version;
        logger = Logger.getLogger(getClass().getName());
        logger.setParent(instance.getLogger());
    }

    /**
     * Process for booting the module.
     */
    protected final void boot()
    {
        logger.info("Enabling module.");

        if (!enabled) {
            try {
                enabled = enable();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (enabled) {
                logger.info("Enabled module.");
            } else {
                logger.warning("Couldn't enable module.");
            }
        }
    }

    /**
     * Process for disabling module.
     */
    protected final void shutdown()
    {
        if (enabled) {
            try {
                disable();
                enabled = false;
                logger.info("Disabled module.");
            } catch (Exception e) {
                logger.warning("Couldn't disable module " + e + ".");
            }
        }
    }

    /**
     * Method signature to inherit when this module is inherited.
     * @return if true, the module enabled successfully, otherwise it didn't.
     * @throws Exception
     */
    protected boolean enable() throws Exception
    {
        return false;
    }

    /**
     * Method signature to inherit when this module is inherited.
     * @throws Exception
     */
    protected void disable() throws Exception
    {
    }
}
