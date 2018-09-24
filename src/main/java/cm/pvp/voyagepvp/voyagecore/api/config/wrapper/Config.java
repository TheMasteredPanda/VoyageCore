package cm.pvp.voyagepvp.voyagecore.api.config.wrapper;

import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * Wrapper for configuration files.
 * @param <T> - generic type.
 */
public class Config<T extends VoyagePlugin>
{
    @Getter
    private File configFile;
    private YamlConfiguration config;

    @Getter
    private VoyagePlugin instance;

    public Config(T instance, File config) throws FileNotFoundException
    {
        this.configFile = config;
        this.instance = instance;

        if (!config.getParentFile().exists()) {
            config.getParentFile().mkdirs();
        }

        if (!config.exists()) {
            instance.saveResource(config.getName(), false);

            if (!config.exists()) {
                throw new FileNotFoundException("Couldn't find file " + config.getName() + " bundled with plugin " + instance.getDescription().getName() + ".");
            }
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Get the wrapped configuration handler.
     * @return
     */
    public YamlConfiguration raw()
    {
        return config;
    }

    /**
     * Inject fields decorated with the annotation @ConfigPopulate in the passed instance.
     * @param instance - instance to populate.
     */
    public void populate(Object instance) throws UnsupportedOperationException
    {
        Class clazz = instance.getClass();
        ArrayList<Field> fields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigPopulate.class)).collect(Collectors.toCollection(Lists::newArrayList));
        fields.addAll(Arrays.stream(clazz.getFields()).filter(f -> f.isAnnotationPresent(ConfigPopulate.class)).collect(Collectors.toCollection(Lists::newArrayList)));

        if (fields.size() == 0) {
            throw new UnsupportedOperationException("No fields decorated with @ConfigPopulate were found in the declared class or any of the inherited classes of " + clazz.getName() + ".");
        }

        for (Field f : fields) {
            ConfigPopulate annotation = f.getAnnotation(ConfigPopulate.class);
            Object value = config.get(annotation.value());

            if (value == null) {
                this.instance.getLogger().warning("The value expected at node '" + annotation.value() + "' that was meant to be injected into field " + f.getName() + " in " + clazz.getName() + " is null.");
                continue;
            }

            try {
                f.setAccessible(true);
                f.set(instance, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
