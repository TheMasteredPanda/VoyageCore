package cm.pvp.voyagepvp.voyagecore.api.command.argument;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.ArgumentCheckFunction;
import lombok.Getter;
import lombok.Setter;

public class ArgumentField
{
    @Getter
    private String name;

    @Getter
    private boolean required;

    @Getter
    private Class type;

    @Setter
    private ArgumentCheckFunction checkFunction;

    public ArgumentField(String name, boolean required, Class type)
    {

    }
}
