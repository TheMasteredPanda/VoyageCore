package cm.pvp.voyagepvp.voyagecore.api.locale;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import org.bukkit.ChatColor;

public final class Format
{
    private Format()
    {
        throw new UtilityException();
    }

    public static String format(String message, String... placeholders)
    {
        for (String placeholder : placeholders) {
            String[] args = placeholder.split(";");
            message = message.replace(args[0], args[1]);
        }

        return message;
    }

    public static String colour(String message)
    {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
