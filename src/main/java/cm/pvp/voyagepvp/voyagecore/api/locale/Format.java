package cm.pvp.voyagepvp.voyagecore.api.locale;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import org.bukkit.ChatColor;

/**
 * Utility for formatting strings.
 */
public final class Format
{
    private Format()
    {
        throw new UtilityException();
    }

    /**
     * Formt a string, replace the placeholders with actual values.
     * @param message - message to format.
     * @param placeholders - placeholder entries and their values. each entry is to be  placeholder;value.
     * @return the formatted message.
     */
    public static String format(String message, String... placeholders)
    {
        for (String placeholder : placeholders) {
            String[] args = placeholder.split(";");
            message = message.replace(args[0], args[1]);
        }

        return message;
    }

    /**
     * A convenience method, colours a message.
     * @param message - message to colour.
     * @return the coloured message.
     */
    public static String colour(String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
