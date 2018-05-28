package cm.pvp.voyagepvp.voyagecore.api.generic;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;

/**
 * Throw an object to a generic type.
 */
public final class GenericUtil
{
    private GenericUtil()
    {
        throw new UtilityException();
    }

    /**
     * Cast an object to a generic type.
     * @param o - object.
     * @param <T> - generic type.
     * @return - generically casted object.
     */
    public static <T> T cast(Object o)
    {
        try {
            return (T) o;
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
