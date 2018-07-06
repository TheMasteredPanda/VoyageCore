package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check.TransferDestinationCheck;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BankTransferCommand extends VoyageCommand
{
    private VEconomy feature;


    @ConfigPopulate("modules.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("modules.veconomy.message.transfersuccess")
    private String transferSuccessMessage;

    @ConfigPopulate("modules.veconomy.message.exceedsmaximumamount")
    private String exceedsMaximumAmountMessage;


    private BankTransferCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.transfer", "Transfer money from one shared account to a player or another shared account.", true, "transfer");
        this.feature = feature;

        ArgumentField bankExists = new ArgumentField("bank name", true);
        ArgumentField destinationExists = new ArgumentField("player name/bank name", true);
        destinationExists.setCheckFunction(new TransferDestinationCheck(feature));
        ArgumentField amountCheck = new ArgumentField("amount", true);
        amountCheck.setCheckFunction(new NumberCheckFunction(double.class));

        try {
            addArguments(bankExists, destinationExists, amountCheck);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        SharedAccount from = feature.getHandler().getAccessibleBanks(p.getUniqueId()).stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);
        double amount = NumberUtil.parse(arguments.get(2), double.class);

        if (from == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (from.getMembers().entrySet().stream().noneMatch(entry -> entry.getKey().equals(p.getUniqueId()) && (entry.getValue() == SharedAccount.Type.POA || entry.getValue() == SharedAccount.Type.OWNER))) {
            sender.sendMessage(Format.colour(Format.format(noPermissionMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (feature.getInstance().getMojangLookup().lookup(arguments.get(1)).isPresent()) {
            VEconomyPlayer to = feature.get(feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId());

            if (from.subtract(amount, p.getUniqueId()).getResponse() == Response.SUCCESS && to.getAccount().add(amount).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + arguments.get(1))));
            }
        } else {
            SharedAccount to = feature.getHandler().getAccessibleBanks(p.getUniqueId()).stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).map(id -> feature.getAccount(id)).findFirst().orElse(null);

            if (to == null) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1))));
                return;
            }

            if (from.subtract(amount, p.getUniqueId()).getResponse() == Response.SUCCESS && to.add(amount, p.getUniqueId()).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + arguments.get(1))));
            }
        }
    }
}
