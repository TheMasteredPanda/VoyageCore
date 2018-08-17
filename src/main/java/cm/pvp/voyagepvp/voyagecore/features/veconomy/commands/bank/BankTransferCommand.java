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
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.PlayerLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.argument.check.TransferDestinationCheck;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.Collectors;

public class BankTransferCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.transfersuccess")
    private String transferSuccessMessage;

    @ConfigPopulate("features.veconomy.messages.bank.exceedsmaximumamount")
    private String exceedsMaximumAmountMessage;

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyBankOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.exceedsminimumamount")
    private String exceedsMinimumAmount;

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
            addArguments(bankExists, new ArgumentField("p or b (p for player, b for bank)", true), destinationExists, amountCheck);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer player = feature.get(p.getUniqueId());
        List<UUID> bankIds = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).collect(Collectors.toCollection(Lists::newArrayList));
        double amount = NumberUtil.parse(arguments.get(3), double.class);

        if (bankIds.size() == 0) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (bankIds.size() > 1) {
            sender.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount};" + String.valueOf(bankIds.size()))));
            return;
        }

        SharedAccount from = feature.getAccount(bankIds.get(0));

        if (arguments.get(1).toLowerCase().equals("b")) {
            List<UUID> targetIds = feature.getHandler().getSharedAccountsNamed(arguments.get(2));
            SharedAccount to = null;
            String[] split = arguments.get(2).split("/");

            if (split.length == 1) {
                if (targetIds.size() == 0) {
                    sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(2))));
                    return;
                }

                if (targetIds.size() > 1) {
                    sender.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount};" + arguments.get(2))));
                    return;
                }

                to = feature.getAccount(targetIds.get(0));
            } else if (split.length == 2) {
                PlayerProfile profile = feature.getInstance().getMojangLookup().lookup(split[0]).get();
                VEconomyPlayer owner = feature.get(profile.getId());


                UUID bankRequested = owner.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equalsIgnoreCase(split[1])).findFirst().orElse(null);

                if (bankRequested == null) {
                    sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + split[1])));
                    return;
                }

                to = feature.getAccount(bankRequested);
            }

            if (to == null) {
                sender.sendMessage(Format.colour(errorMessage));
                return;
            }

            if (!from.isMember(p.getUniqueId()) || from.getMembers().get(p.getUniqueId()) == SharedAccount.Type.MEMBER) {
                sender.sendMessage(Format.colour(noPermissionMessage));
                return;
            }

            if (Double.isInfinite(to.getBalance() + amount)) {
                sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmountMessage, "{amount};" + String.valueOf(amount), "{receiver};bank " + to.getName())));
                return;
            }

            if ((from.getBalance() - amount) < 0) {
                sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmountMessage, "{amount};" + String.valueOf(amount), "{receiver};bank " + to.getName())));
                return;
            }

            if (from.subtract(amount).getResponse() == Response.SUCCESS && to.add(amount).getResponse() == Response.SUCCESS) {
                feature.getHandler().addSharedLedgerEntry(from.getId(), new SharedLedgerEntry(from.getId(), Action.WITHDRAW_MONEY, p.getUniqueId(), from.getBalance(), amount, new Date(), ImmutableMap.<String, Object>builder().put("destinationIsBank", true).put("destination", to.getName()).build()));
                feature.getHandler().addSharedLedgerEntry(to.getId(), new SharedLedgerEntry(to.getId(), Action.DEPOSIT_MONEY, p.getUniqueId(), to.getBalance(), amount, new Date(), ImmutableMap.<String, Object>builder().put("originIsBank", true).put("origin", from.getName()).build()));
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver}; bank" + to.getName())));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        } else if (arguments.get(1).toLowerCase().equals("p")) {
            Optional<PlayerProfile> optional = feature.getInstance().getMojangLookup().lookup(arguments.get(2));

            if (!optional.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{target};" + arguments.get(2))));
                return;
            }

            PlayerProfile profile = optional.get();
            VEconomyPlayer target = feature.get(profile.getId());

            if ((from.getBalance() - amount) < 0) {
                sender.sendMessage(Format.colour(Format.format(exceedsMinimumAmount, "{amount};" + String.valueOf(amount), "{receiver};" + profile.getName())));
                return;
            }

            if (!from.isMember(p.getUniqueId()) || from.getMembers().get(p.getUniqueId()) == SharedAccount.Type.MEMBER) {
                sender.sendMessage(Format.colour(noPermissionMessage));
                return;
            }

            if (from.subtract(amount).getResponse() == Response.SUCCESS && target.getAccount().add(amount).getResponse() == Response.SUCCESS) {
                feature.getHandler().addSharedLedgerEntry(from.getId(), new SharedLedgerEntry(from.getId(), Action.WITHDRAW_MONEY, p.getUniqueId(), from.getBalance(), amount, new Date(), ImmutableMap.<String, Object>builder().put("destinationIsBank", false).put("destination", target.getAccount().getOwner().toString()).build()));
                feature.getHandler().addPlayerLedgerEntry(target.getAccount().getOwner(), new PlayerLedgerEntry(target.getAccount().getOwner(), Action.DEPOSIT_MONEY, amount, target.getAccount().getBalance(), new Date(), ImmutableMap.<String, Object>builder().put("originIsBank", true).put("origin", feature.getInstance().getMojangLookup().lookup(from.getOwner()).get().getName() + "/" + from.getName()).build()));
                sender.sendMessage(Format.colour(Format.format(transferSuccessMessage, "{amount};" + String.valueOf(amount), "{receiver};" + profile.getName())));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        }
    }
}
