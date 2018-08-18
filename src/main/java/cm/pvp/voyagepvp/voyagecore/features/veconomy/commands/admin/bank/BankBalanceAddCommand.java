package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.NumberCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankBalanceAddCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.exceedsmaximumamount")
    private String exceedsMaximumAmoutnMessage;

    @ConfigPopulate("features.veconomy.messages.admin.bank.addedmoney")
    private String addedMoneyMessage;

    public BankBalanceAddCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.balance.add", "Add money to a bank.", false, "add");
        this.feature = feature;

        ArgumentField ownerArg = new ArgumentField("owner's name (player name)", true);
        ownerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));
        ArgumentField amountArg = new ArgumentField("amount", true);
        amountArg.setCheckFunction(new NumberCheckFunction(double.class));

        try {
            addArguments(ownerArg, new ArgumentField("bank name", true), amountArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer owner = feature.get(feature.getInstance().getBackupLookup().lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = owner.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(owner.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1))));
            return;
        }

        double amount = NumberUtil.parse(arguments.get(2), double.class);
        SharedAccount account = ownedBanks.stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).map(id -> feature.getAccount(id)).findFirst().get();

        if (Double.isInfinite(account.getBalance() + amount) || (account.getBalance() + amount) > feature.getSharedAccountMaximumBalance()) {
            sender.sendMessage(Format.colour(Format.format(exceedsMaximumAmoutnMessage, "{amount};" + feature.getVaultHook().format(amount), "{receiver};" + arguments.get(1))));
            return;
        }

        if (account.add(amount).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(addedMoneyMessage, "{amount};" + feature.getVaultHook().format(amount), "{bank};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
