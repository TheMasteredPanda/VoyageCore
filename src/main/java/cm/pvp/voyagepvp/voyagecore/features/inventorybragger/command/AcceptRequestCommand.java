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
import org.bukkit.inventory.PlayerInventory;

import java.util.LinkedList;

public class AcceptRequestCommand extends VoyageCommand
{
    private InventoryBragger feature;

    @ConfigPopulate("features.inventorybragger.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.inventorybragger.messages.requestaccepted")
    private String requestAcceptedMessage;

    public AcceptRequestCommand(InventoryBragger feature)
    {
        super(null, "voyagecore.inventorybragger.acceptrequest", "Accept a request from a player to view your inventory.", true, "accept");
        this.feature = feature;

        addArguments(new ArgumentField("player name", true).check(new PlayerCheckFunction(feature.getInstance().getBackupLookup())));
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        Player target = Players.get(arguments.get(0));

        if (target == null || !feature.getRequests().containsEntry(p.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + arguments.get(0))));
            return;
        }

        feature.getRequests().remove(p.getUniqueId(), target.getUniqueId());
        PlayerInventory inv = p.getInventory();
        feature.getViewing().put(target.getUniqueId(), inv);
        target.openInventory(inv);
    }
}
