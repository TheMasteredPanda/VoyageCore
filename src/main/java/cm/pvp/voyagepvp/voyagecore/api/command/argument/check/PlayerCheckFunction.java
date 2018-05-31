package cm.pvp.voyagepvp.voyagecore.api.command.argument.check;

import cm.pvp.voyagepvp.voyagecore.api.lookup.Lookup;

/**
 * Check if the inputted player name is correct.
 */
public class PlayerCheckFunction implements ArgumentCheckFunction
{
    private Lookup lookup;

    public PlayerCheckFunction(Lookup lookup)
    {
        this.lookup = lookup;
    }

    @Override
    public boolean check(String argument)
    {
        return lookup.lookup(argument).isPresent();
    }
}
