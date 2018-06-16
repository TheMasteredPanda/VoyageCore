package cm.pvp.voyagepvp.voyagecore.api.command.argument;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.ArgumentCheckFunction;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ArgumentField
{
    private String name;
    private boolean required;

    @Setter
    private ArgumentCheckFunction checkFunction;

    public ArgumentField(String name, boolean required)
    {
        this.name = name;
        this.required = required;
    }
}
