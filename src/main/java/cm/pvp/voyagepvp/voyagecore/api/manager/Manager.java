package cm.pvp.voyagepvp.voyagecore.api.manager;

import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.logging.Logger;

/**
 * Template class for Managers.
 * @param <T>
 */
@Getter
public class Manager<T extends VoyagePlugin>
{
    private String name;
    private double version;
    private boolean enabled = false;

    @Getter(value = AccessLevel.PROTECTED)
    private Logger logger;

    public Manager(T instance, String name, double version)
    {
        this.name = name;
        this.version = version;
        logger = Logger.getLogger("Manager: " + getClass().getSimpleName());
        logger.setParent(instance.getLogger());
    }

    /**
     * Process to boot the manager.
     */
    public final void boot()
    {
        if (!enabled) {
            logger.info("Enabling manager.");

            try {
                enabled = enable();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (enabled) {
                logger.info("Manager enabled!");
            } else {
                logger.warning("Manager didn't enable correctly. Module not enabled.");
            }
        }
    }

    /**
     * Process to shutdown the manager.
     */
    public final void shutdown()
    {
        if (enabled) {
            logger.info("Disabling manager.");

            try {
                disable();
                logger.info("Disabled manager.");
                enabled = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method signature to be inherited when this class is inherited.
     * @return if true the Manager enabled successfully, else the Manager
     * enabled unsuccessfully.
     * @throws Exception
     */
    public boolean enable() throws Exception
    {
        return true;
    }

    /**
     * Method signature to be inherited when this class is inherited.
     * @throws Exception
     */
    public void disable() throws Exception
    {
    }
}
