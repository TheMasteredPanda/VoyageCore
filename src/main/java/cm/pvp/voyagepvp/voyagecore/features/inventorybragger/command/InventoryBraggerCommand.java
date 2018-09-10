package cm.pvp.voyagepvp.voyagecore.features.inventorybragger.command;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.inventorybragger.InventoryBragger;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class InventoryBraggerCommand extends VoyageCommand
{
    public InventoryBraggerCommand(InventoryBragger feature)
    {
        super(null, null, "Parent command for the InventoryBragger module.", true, "invbragger", "invb");
        addChildren(new ViewCommand(feature), new AcceptRequestCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {

    }
}
