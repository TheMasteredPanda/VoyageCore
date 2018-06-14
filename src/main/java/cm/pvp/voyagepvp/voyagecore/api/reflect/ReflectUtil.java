package cm.pvp.voyagepvp.voyagecore.api.reflect;

import cm.pvp.voyagepvp.voyagecore.api.exception.ReflectionException;
import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.ConstructorAccessor;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.FieldAccessor;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.MethodAccessor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ReflectUtil
{
    private static Cache<String, Object> reflections = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private static String OBC = "org.bukkit.craftbukkit." + getServerVersion() + ".";
    private static String NMS = "net.minecraft.server." + getServerVersion() + ".";

    private ReflectUtil()
    {
        throw new UtilityException();
    }

    public static Class getClass(String name)
    {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(e);
        }
    }

    public static Class getOBCClass(String className)
    {
        return getClass(OBC + className);
    }

    public static Class getNMSClass(String className)
    {
        return getClass(NMS + className);
    }

    public static String getServerVersion()
    {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static MethodAccessor getMethod(Class origin, String name, boolean declared, Class... parameters)
    {
        StringBuilder sb = new StringBuilder("M/").append(origin.getCanonicalName()).append("/").append(name).append("/").append(declared);

        for (Class parameter : parameters) {
            sb.append(parameter.getName());

            if (parameters[parameters.length - 1] != parameter) {
                sb.append(";");
            }
        }

        if (reflections.asMap().containsKey(sb.toString())) {
            return GenericUtil.cast(reflections.getIfPresent(sb.toString()));
        }

        try {
            Method method = declared ? origin.getDeclaredMethod(name, parameters) : origin.getMethod(name, parameters);
            MethodAccessor accessor = new MethodAccessor(method);
            reflections.put(sb.toString(), accessor);
            return accessor;
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        }
    }

    public static FieldAccessor getField(Class origin, String name, boolean declared)
    {
        StringBuilder sb = new StringBuilder("F/").append(origin.getCanonicalName()).append("/").append(name).append("/").append(declared);

        if (reflections.asMap().containsKey(sb.toString())) {
            return GenericUtil.cast(reflections.getIfPresent(sb.toString()));
        }

        try {
            Field field = declared ? origin.getDeclaredField(name) : origin.getField(name);
            FieldAccessor accessor = new FieldAccessor(field);
            reflections.put(sb.toString(), accessor);
            return accessor;
        } catch (NoSuchFieldException e) {
            throw new ReflectionException(e);
        }
    }

    public static ConstructorAccessor getConstructor(Class origin, boolean declared, Class... parameters)
    {
        StringBuilder sb = new StringBuilder("C/").append(origin).append("/").append(declared);

        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getName());

            if (parameters[parameters.length - 1] != parameters[i]) {
                sb.append(";");
            }
        }

        if (reflections.asMap().containsKey(sb.toString())) {
            return GenericUtil.cast(reflections.getIfPresent(sb.toString()));
        }

        try {
            Constructor constructor = declared ? origin.getDeclaredConstructor(parameters) : origin.getConstructor(parameters);
            ConstructorAccessor accessor = new ConstructorAccessor(constructor);
            reflections.put(sb.toString(), constructor);
            return accessor;
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        }
    }

    public static Cache<String, Object> getCache()
    {
        return reflections;
    }
}