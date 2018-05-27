package cm.pvp.voyagepvp.voyagecore.api.command.argument;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArgumentField
{
    private String name;
    private boolean required;
    private Class type;
}
