package cm.pvp.voyagepvp.voyagecore.api.exception;

public class MojangException extends RuntimeException
{
    public MojangException(String message)
    {
        super(message);
    }

    public MojangException(Throwable t)
    {
        super(t);
    }
}
