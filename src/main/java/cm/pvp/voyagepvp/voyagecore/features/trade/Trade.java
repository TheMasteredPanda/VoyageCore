package cm.pvp.voyagepvp.voyagecore.features.trade;

import cm.pvp.voyagepvp.voyagecore.Feature;

public class Trade extends Feature
{
    public Trade()
    {
        super("VTrade", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new TradeCommand(this));
        return true;
    }
}
