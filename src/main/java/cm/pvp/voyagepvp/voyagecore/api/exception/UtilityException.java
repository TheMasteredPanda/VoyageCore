package cm.pvp.voyagepvp.voyagecore.api.exception;

public class UtilityException extends RuntimeException
{
    public UtilityException()
    {
        super("You cannot instantiate a utility class");
    }

    public UtilityException(String message)
    {
        super(message);
    }
}
