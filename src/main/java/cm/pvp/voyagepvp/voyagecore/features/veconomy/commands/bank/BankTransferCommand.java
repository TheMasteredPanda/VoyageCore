package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check.TransferDestinationCheck;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @ConfigPopulate("modules.veconomy.messages.bank.specifybankowner")
    private String specifyBankOwnerMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.error")
    private String errorMessage;

    public BankTransferCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.transfer", "Transfer money from one shared account to a player or another shared account.", true, "transfer");
        this.feature = feature;

        ArgumentField bankExists = new ArgumentField("bank name", true);
        ArgumentField destinationExists = new ArgumentField("player name/(bank name or owner/bank name e.g. themasteredpanda/savings", true);
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
        VEconomyPlayer player = feature.get(p.getUniqueId());
        SharedAccount from = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);
        double amount = NumberUtil.parse(arguments.get(2), double.class);

        if (from == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (from.getMembers().get(p.getUniqueId()) != SharedAccount.Type.POA || !from.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(Format.format(noPermissionMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (feature.getInstance().getMojangLookup().lookup(arguments.get(1)).isPresent()) {
            VEconomyPlayer to = feature.get(feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId());

            if (from.subtract(amount, p.getUniqueId()).getResponse() == Response.SUCCESS && to.getAccount().add(amount).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + arguments.get(1))));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        } else {
            SharedAccount to = null;
            PlayerProfile owner = null;
            String[] split = arguments.get(1).split("/");

            if (split.length == 1) {
                List<UUID> accounts = feature.getHandler().getSharedAccountsNamed(split[0]);

                if (accounts.size() > 1) {
                    sender.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount};" + accounts.size())));
                    return;
                } else if (accounts.size() == 1) {
                    to = feature.getAccount(accounts.get(0));
                    owner = feature.getInstance().getMojangLookup().lookup(to.getId()).get();
                }
            } else {
                List<UUID> accounts = from.getHandler().getSharedAccountsNamed(split[1]);

                to = accounts.stream().filter(id -> feature.getAccount(id).getName().equals(split[1])).map(id -> feature.getAccount(id)).findFirst().orElse(null);

                if (to == null) {
                    sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + split[1])));
                    return;
                } else {
                    Optional<PlayerProfile> optional = feature.getInstance().getMojangLookup().lookup(split[0]);

                    if (!optional.isPresent()) {
                        sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{target};" + split[0])));
                        return;
                    }

                    owner = optional.get();
                }
            }

            Preconditions.checkNotNull(to);
            Preconditions.checkNotNull(owner);

            if (from.subtract(amount, p.getUniqueId()).getResponse() == Response.SUCCESS && to.add(amount, p.getUniqueId()).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + owner.getName())));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }

        }
    }
}
