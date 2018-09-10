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

public class ViewCommand extends VoyageCommand
{
    private InventoryBragger feature;

    @ConfigPopulate("features.inventorybragger.messages.sendingrequest")
    private String sendingRequestMessage;

    @ConfigPopulate("features.inventorybragger.messages.requestalreadysent")
    private String requestAlreadySentMessage;

    @ConfigPopulate("features.inventorybragger.messages.targetnotfound")
    private String targetNotfoundMessage;

    @ConfigPopulate("features.inventorybragger.messages.cannotselfview")
    private String cannotSelfViewMessage;

    public ViewCommand(InventoryBragger feature)
    {
        super(null, "voyagecore.inventorybragger.view", "Request to view a players inventory.", true, "view");
        this.feature = feature;

        try {
            ArgumentField playerArg = new ArgumentField("player name", true);
            playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));
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

        if (target == null || !target.isOnline()) {
            p.sendMessage(Format.colour(Format.format(targetNotfoundMessage, "{player};" + arguments.get(0))));
            return;
        }

        if (target.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage(Format.colour(cannotSelfViewMessage));
            return;
        }

        if (!feature.getRequests().containsEntry(target.getUniqueId(), p.getUniqueId())) {
            p.sendMessage(Format.colour(Format.format(sendingRequestMessage, "{player};" + arguments.get(0))));
            feature.getRequests().put(p.getUniqueId(), target.getUniqueId());
        } else {
            p.sendMessage(Format.colour(Format.format(requestAlreadySentMessage, "{player};" + target.getName())));
        }
    }
}
