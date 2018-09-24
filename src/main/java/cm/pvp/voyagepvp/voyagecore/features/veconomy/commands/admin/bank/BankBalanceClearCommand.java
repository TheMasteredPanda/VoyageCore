package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankBalanceClearCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.admin.clearedbank")
    private String clearedBankMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;


    public BankBalanceClearCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.balance.clear", "Clear a banks balance.", false, "clear");
        this.feature = feature;
        addArguments(new ArgumentField("owner's name (player name)", true).check(new PlayerCheckFunction(feature.getInstance().getBackupLookup())), new ArgumentField("bank name", true));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getBackupLookup().lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(player.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1), "{target};" + arguments.get(0))));
            return;
        }

        UUID accountId = ownedBanks.stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).findFirst().get();

        SharedAccount account = feature.getAccount(accountId);

        if (account.subtract(account.getBalance()).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(clearedBankMessage, "{bank};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
