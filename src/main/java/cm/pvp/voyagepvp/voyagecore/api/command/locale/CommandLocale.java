package cm.pvp.voyagepvp.voyagecore.api.command.locale;

public interface CommandLocale
{
    String get(Key key);

    String getCommandPrefix();

    public static enum Key
    {
        NO_PERMISSION,
        PLAYER_ONLY_COMMAND,
        COMMAND_PREFIX,
        NOT_ENOUGH_ARGUMENTS,
        ARGUMENT_INCORRECT,
        HELP_TEMPLATE,
        HELP_COMMAND_ENTRY,
        HELP_COMMAND_ENTRY_DESCRIPTION
    }
}
