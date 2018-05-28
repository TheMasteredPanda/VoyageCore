package cm.pvp.voyagepvp.voyagecore.api.command.argument.check;

import java.util.regex.Pattern;

/**
 * Argument check function to check if a string matches the regular expression specified.
 */
public class RegexCheckFunction implements ArgumentCheckFunction
{
    private Pattern regex;

    public RegexCheckFunction(String regex)
    {
        this.regex = Pattern.compile(regex);
    }

    @Override
    public boolean check(String argument)
    {
        return regex.matcher(argument).matches();
    }
}
