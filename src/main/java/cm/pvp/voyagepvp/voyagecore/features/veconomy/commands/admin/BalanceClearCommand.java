package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BalanceClearCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.admin.clearedbalance")
    private String clearedBalanceMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    public BalanceClearCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.balance.clear", "Clear a players balance", true, "clear");
        this.feature = feature;

        ArgumentField playerArg = new ArgumentField("player name", true);

        try {
            addArguments(playerArg);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getMojangLookup().lookup(arguments.get(0)).get().getId());

        if (player.getAccount().subtract(player.getAccount().getBalance()).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(clearedBalanceMessage, "{target};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
