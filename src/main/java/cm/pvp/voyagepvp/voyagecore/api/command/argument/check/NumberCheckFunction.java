package cm.pvp.voyagepvp.voyagecore.api.command.argument.check;

import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;

/**
 * Check if the type of number expected has been inputted.
 */
public class NumberCheckFunction implements ArgumentCheckFunction
{
    private Class<? extends Number> type;

    public NumberCheckFunction(Class<? extends Number> type)
    {
        this.type = type;
    }

    @Override
    public boolean check(String argument)
    {
        return NumberUtil.parseable(argument, type);
    }
}
