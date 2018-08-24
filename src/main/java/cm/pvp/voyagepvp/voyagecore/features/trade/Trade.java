package cm.pvp.voyagepvp.voyagecore.features.trade;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;

public class Trade extends Feature
{
    public Trade(VoyageCore instance)
    {
        super(instance, "VTrade", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new TradeCommand(this));
        return true;
    }
}
