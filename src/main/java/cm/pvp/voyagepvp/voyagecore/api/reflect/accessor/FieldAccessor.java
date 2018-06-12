package cm.pvp.voyagepvp.voyagecore.api.reflect.accessor;

import cm.pvp.voyagepvp.voyagecore.api.exception.ReflectionException;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;

import java.lang.reflect.Field;

/**
 * Wrapper for a field.
 * @param <T>
 */
public class FieldAccessor<T>
{
    private Field field;

    public FieldAccessor(Field field)
    {
        field.setAccessible(true);
        this.field = field;
    }

    /**
     * Set the value in the field from a particular instance containing the field.
     * @param instance - the instance containing the field.
     * @param value - value to assign to this field.
     */
    public void set(Object instance, T value)
    {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Get the value from the field in a particular instance containing the field.
     * @param instance - the instance containing the field.
     * @return the value within the field.
     */
    public T get(Object instance)
    {
        try {
            return GenericUtil.cast(field.get(instance));
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }
}
