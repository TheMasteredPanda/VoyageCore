package cm.pvp.voyagepvp.voyagecore.api.exception;

public class CommandException extends RuntimeException
{
    public CommandException(String message)
    {
        super(message);
    }

    public CommandException(Throwable t)
    {
        super(t);
    }
}
