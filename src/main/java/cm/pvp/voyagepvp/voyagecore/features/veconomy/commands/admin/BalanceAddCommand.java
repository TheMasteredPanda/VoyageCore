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

public class BalanceAddCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.admin.gavemoney")
    private String gaveMoneyMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.exceedmaximumamount")
    private String exceedsMaximumAmountMessage;

    public BalanceAddCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.balance.add", "Add money to a players balance.", false, "add");
        this.feature = feature;

        ArgumentField numberArg = new ArgumentField("amount", true);
        numberArg.setCheckFunction(new NumberCheckFunction(double.class));
        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(playerArg, numberArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getMojangLookup().lookup(arguments.get(0)).get().getId());
        double amount = NumberUtil.parse(arguments.get(1), double.class);

        if (Double.isInfinite(player.getAccount().getBalance() + amount)) {
            sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmountMessage, "{amount};" + feature.getVaultHook().format(amount), "{receiver};" + arguments.get(0))));
            return;
        }

        if (player.getAccount().add(amount).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(gaveMoneyMessage, "{amount};" + feature.getVaultHook().format(amount), "{receiver};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
