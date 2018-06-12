package cm.pvp.voyagepvp.voyagecore.api.reflect.accessor;

import cm.pvp.voyagepvp.voyagecore.api.exception.ReflectionException;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Wrapper for a constructor.
 * @param <T> - generic type.
 */
public class ConstructorAccessor<T>
{
    private Constructor constructor;

    public ConstructorAccessor(Constructor constructor)
    {
        constructor.setAccessible(true);
        this.constructor = constructor;
    }

    /**
     * Create a new instance.
     * @param parameters - parameters for the constructor.
     * @return an instance made by the invocation of the constructor.
     */
    public T invoke(Object... parameters)
    {
        try {
            return GenericUtil.cast(constructor.newInstance(parameters));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ReflectionException(e);
        }
    }
}
