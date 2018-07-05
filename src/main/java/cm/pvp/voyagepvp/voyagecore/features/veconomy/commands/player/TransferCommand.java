package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check.TransferDestinationCheck;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class TransferCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("modules.veconomy.message.transfersuccess")
    private String transferSuccessMessage;

    @ConfigPopulate("modules.veconomy.message.exceedsmaximumamount")
    private String exceedsMaximumAmountMessage;

    @ConfigPopulate("modules.veconomy.message.notenoughmoney")
    private String notEnoughMoneyMessage;

    @ConfigPopulate("modules.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public TransferCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.tranfer", "Transfer money from one bank to another", false, "transfer");
        this.feature = feature;
        ArgumentField field = new ArgumentField("player name/bank account name", true);
        field.setCheckFunction(new TransferDestinationCheck(feature));
        ArgumentField amountArg = new ArgumentField("amount", true);
        amountArg.setCheckFunction(new NumberCheckFunction(double.class));

        try {
            addArguments(field, amountArg);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        MojangLookup lookup = feature.getInstance().getMojangLookup();
        VEconomyPlayer player = feature.get((Player) sender);
        PlayerAccount playerAccount = player.getAccount();

        double balance = NumberUtil.parse(arguments.get(1), double.class);

        if ((playerAccount.getBalance() - balance) < 0) {
            sender.sendMessage(Format.colour(notEnoughMoneyMessage));
            return;
        }

        if (lookup.lookup(arguments.get(0)).isPresent()) {
            VEconomyPlayer target = feature.get(lookup.lookup(arguments.get(0)).get().getId());

            if (target == null) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{target};" + arguments.get(0))));
                return;
            }

            if (playerAccount.subtract(balance).getResponse() == Response.SUCCESS && target.getAccount().add(balance).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(balance), "{receiver};" + arguments.get(0))));
            }
        } else {
            SharedAccount target = feature.getHandler().getAccessibleBanks(lookup.lookup(arguments.get(0)).get().getId()).stream().filter(uuid -> feature.getAccount(uuid).getName().equals(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);

            if (target == null) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{target};" + arguments.get(0))));
                return;
            }

            if (playerAccount.subtract(balance).getResponse() == Response.SUCCESS && target.add(balance, player.getReference().get().getUniqueId()).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(balance), "{receiver};" + target.getName())));
            }
        }
    }
}
