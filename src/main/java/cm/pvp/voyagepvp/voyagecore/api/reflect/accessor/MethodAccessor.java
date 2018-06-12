package cm.pvp.voyagepvp.voyagecore.api.reflect.accessor;

import cm.pvp.voyagepvp.voyagecore.api.exception.ReflectionException;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper for a method.
 * @param <T> - generic type.
 */
public class MethodAccessor<T>
{
    private Method method;

    public MethodAccessor(Method m)
    {
        m.setAccessible(true);
        method = m;
    }

    /**
     * Invoke the method instance.
     * @param instance - instance to invoke the method from.
     * @param parameters - parameters for the method.
     * @return whatever the result of the method is.
     */
    public T invoke(Object instance, Object... parameters)
    {
        try {
            return GenericUtil.cast(method.invoke(instance, parameters));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * @return the raw method.
     */
    public Method getMethod()
    {
        return method;
    }
}
