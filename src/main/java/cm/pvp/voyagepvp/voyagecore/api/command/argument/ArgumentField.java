package cm.pvp.voyagepvp.voyagecore.api.command.argument;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.ArgumentCheckFunction;
import lombok.Getter;

@Getter
public class ArgumentField
{
    private String name;
    private boolean required;
    private ArgumentCheckFunction checkFunction;

    public ArgumentField(String name, boolean required)
    {
        this.name = name;
        this.required = required;
    }

    public ArgumentField check(ArgumentCheckFunction function)
    {
        this.checkFunction = function;
        return this;
    }
}
