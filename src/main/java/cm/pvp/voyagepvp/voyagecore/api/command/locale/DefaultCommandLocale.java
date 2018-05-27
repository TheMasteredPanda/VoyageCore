package cm.pvp.voyagepvp.voyagecore.api.command.locale;

import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import com.google.common.collect.ImmutableMap;

/**
 * Default locale manager for commands.
 */
public class DefaultCommandLocale implements CommandLocale
{
    private ImmutableMap<Key, String> locale;

    public DefaultCommandLocale()
    {
        locale = ImmutableMap.<Key, String>builder()
                .put(Key.COMMAND_PREFIX, "&aCommand &8» &7")
                .put(Key.NO_PERMISSION, "{commandPrefix} &cNo permission.")
                .put(Key.NOT_ENOUGH_ARGUMENTS, "{commandPrefix} Not enough arguments for {commandusage}")
                .put(Key.PLAYER_ONLY_COMMAND, "{commandPrefix} Player only command.")
                .build();
    }

    /**
     * Get a locale entry.
     * @param key - key.
     * @return the locale entry if found, else it will throw NullPointerException.
     */
    @Override
    public String get(Key key)
    {
        if (!locale.containsKey(key)) {
            throw new NullPointerException("Can't find locale entry known by key " + key + ".");
        }

        return Format.format(locale.get(key), "{commandPrefix};" + getCommandPrefix());
    }

    /**
     * Get the command prefix.
     * @return the command prefix.
     */
    @Override
    public String getCommandPrefix()
    {
        return locale.get(Key.COMMAND_PREFIX);
    }
}
