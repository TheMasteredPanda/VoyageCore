package cm.pvp.voyagepvp.voyagecore.features.customprefix;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.CustomPrefixAdminCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.CustomPrefixCommand;
import lombok.Getter;


//TODO add blacklisted words.
@Getter
public class CustomPrefix extends Feature
{
    public CustomPrefix(VoyageCore instance)
    {
        super(instance, "CustomPrefix", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new CustomPrefixCommand(getInstance(), this), new CustomPrefixAdminCommand(getInstance(), this));
        return true;
    }
}
