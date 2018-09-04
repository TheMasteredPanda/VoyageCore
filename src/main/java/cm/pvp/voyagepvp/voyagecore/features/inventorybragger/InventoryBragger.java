package cm.pvp.voyagepvp.voyagecore.features.inventorybragger;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;

public class InventoryBragger extends Feature
{
    public InventoryBragger(VoyageCore instance)
    {
        super(instance, "InventoryBragger", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {

        return true;
    }
}
