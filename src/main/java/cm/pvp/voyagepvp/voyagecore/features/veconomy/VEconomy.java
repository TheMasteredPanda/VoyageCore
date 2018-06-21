package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;

public class VEconomy extends Feature
{
    private DataHandler handler;

    public VEconomy(VoyageCore instance)
    {
        super(instance, "VEconomy", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        handler = new DataHandler(getInstance());
        return super.enable();
    }
}
