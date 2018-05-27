package cm.pvp.voyagepvp.voyagecore.api.config.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to denote fields in a class
 * to be injected with the value in a configuration
 * file found at the specified node path.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigPopulate
{
    /**
     * The defined path of the value the field will be injected with.
     * @return node path.
     */
    String value();
}
