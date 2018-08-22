package cm.pvp.voyagepvp.voyagecore.features.trade;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class TradeCommand extends VoyageCommand
{
    private Trade feature;

    @ConfigPopulate("features.vtrade.messages.playernotonline")
    private String playerNotOnlineMessage;

    @ConfigPopulate("features.vtrade.messages.tradesessionstarted")
    private String tradeSessionStartedMessage;

    public TradeCommand(Trade feature)
    {
        super(null, "voyagecore.vtrade.trade", "Open up a trade gui with another player.", true, "trade");

        try {

            ArgumentField field = new ArgumentField("player name", true);
            field.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));
            addArguments(field);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (!Players.get(arguments.get(0)).isOnline()) {
            sender.sendMessage(Format.colour(Format.format(playerNotOnlineMessage, "{target};" + arguments.get(0))));
        }

        Player p = (Player) sender;
        Player target = Players.get(arguments.get(0));
        p.sendMessage(tradeSessionStartedMessage);
        new TradeGUI(p, target);
    }
}
