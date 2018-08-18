package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BalanceRemoveCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.exceedsminimumamount")
    private String exceedsMinimumAmountMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.admin.removedmoney")
    private String removedMoneyMessage;

    public BalanceRemoveCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.balance.remove", "Remove a sum of money from a players balance", false, "remove");
        this.feature = feature;

        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));
        ArgumentField amountArg = new ArgumentField("amount", true);
        amountArg.setCheckFunction(new NumberCheckFunction(double.class));

        try {
            addArguments(playerArg, amountArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getBackupLookup().lookup(arguments.get(0)).get().getId());

        double amount = NumberUtil.parse(arguments.get(1), double.class);

        if (player.getAccount().getBalance() < amount) {
            sender.sendMessage(Format.colour(Format.format(exceedsMinimumAmountMessage, "{amount};" + feature.getVaultHook().format(amount), "{receiver};" + arguments.get(0))));
            return;
        }

        if (player.getAccount().subtract(amount).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(removedMoneyMessage, "{amount};" + feature.getVaultHook().format(amount), "{target};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
