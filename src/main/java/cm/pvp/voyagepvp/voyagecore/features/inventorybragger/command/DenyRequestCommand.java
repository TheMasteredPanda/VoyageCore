package cm.pvp.voyagepvp.voyagecore.features.inventorybragger.command;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import cm.pvp.voyagepvp.voyagecore.features.inventorybragger.InventoryBragger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class DenyRequestCommand extends VoyageCommand
{
    private InventoryBragger feature;

    @ConfigPopulate("feature.inventorybragger.messages.playernotfound")
    private String playerNotFoundMesage;

    @ConfigPopulate("feature.inventorybragger.messages.requestremoved")
    private String removedRequestMessage;

    public DenyRequestCommand(InventoryBragger feature)
    {
        super(null, "voyagecore.inventorybragger.denyrequest", "Deny a player's request to view your inventory.", true, "deny");
        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));

        try {
            addArguments(playerArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        Player target = Players.get(arguments.get(0));

        if (p == null || !feature.getRequests().containsEntry(target.getUniqueId(), p.getUniqueId())) {
            sender.sendMessage(Format.colour(Format.format(playerNotFoundMesage, "{player};" + arguments.get(0))));
            return;
        }

        feature.getRequests().remove(target.getUniqueId(), p.getUniqueId());
        sender.sendMessage(Format.colour(removedRequestMessage));

    }
}
