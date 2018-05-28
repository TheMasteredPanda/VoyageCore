package cm.pvp.voyagepvp.voyagecore.api.math;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import cm.pvp.voyagepvp.voyagecore.api.generic.GenericUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility to parse strings into numbers.
 */
public final class NumberUtil
{
    private NumberUtil()
    {
        throw new UtilityException();
    }

    /**
     * Check if a string can be parsable with that type.
     * @param o - object to parse.
     * @param type - type of number to parse the string to.
     * @param <T> - generic number type.
     * @return true if parseable, else false.
     */
    public static <T extends Number> boolean parseable(String o, Class<T> type)
    {
        try {
            parse(o, type);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parse a string into a number.
     * @param o - object to parse.
     * @param type - type of number to parse the string to.
     * @param <T> - generic number type.
     * @return the parsed number.
     */
    public static <T extends Number> T parse(String o, Class<T> type)
    {
        if (type.equals(byte.class)) {
            return GenericUtil.cast(Byte.parseByte(o));
        }

        if (type.equals(short.class)) {
            return GenericUtil.cast(Short.parseShort(o));
        }

        if (type.equals(int.class)) {
            return GenericUtil.cast(Integer.parseInt(o));
        }

        if (type.equals(long.class)) {
            return GenericUtil.cast(Long.parseLong(o));
        }

        if (type.equals(float.class)) {
            return GenericUtil.cast(Float.parseFloat(o));
        }

        if (type.equals(double.class)) {
            return GenericUtil.cast(Double.parseDouble(o));
        }

        if (type.equals(BigDecimal.class)) {
            return GenericUtil.cast(new BigDecimal(o));
        }

        if (type.equals(BigInteger.class)) {
            return GenericUtil.cast(new BigDecimal(o));
        }

        throw new NumberFormatException("Format specified is not supported.");
    }
}
