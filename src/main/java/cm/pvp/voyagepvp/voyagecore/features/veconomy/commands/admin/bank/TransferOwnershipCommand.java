package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.Lookup;
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

public class TransferOwnershipCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.transferredownership")
    private String transferredOwnershipMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisowner")
    private String targetIsOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String targetIsNotMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public TransferOwnershipCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.transferownership", "Transfer the ownership of a bank to a member of that bank.", false, "transfer");
        this.feature = feature;

        addArguments(new ArgumentField("owner's name (player name)", true).check(new PlayerCheckFunction(feature.getInstance().getBackupLookup()))
                , new ArgumentField("bank name", true), new ArgumentField("new owner's name (player name)", true).check(new PlayerCheckFunction(feature.getInstance().getBackupLookup())));
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Lookup lookup = feature.getInstance().getBackupLookup();
        VEconomyPlayer owner = feature.get(lookup.lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = owner.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(owner.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1), "{target};" + arguments.get(0))));
            return;
        }

        SharedAccount account = ownedBanks.stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).map(id -> feature.getAccount(id)).findFirst().get();
        VEconomyPlayer newOwner = feature.get(lookup.lookup(arguments.get(2)).get().getId());

        if (!account.isMember(newOwner.getReference().get().getUniqueId())) {
            sender.sendMessage(Format.colour(Format.format(targetIsNotMemberMessage, "{target};" + arguments.get(2))));
            return;
        }

        if (newOwner.getReference().get().getUniqueId().equals(account.getOwner())) {
            sender.sendMessage(Format.colour(Format.format(targetIsOwnerMessage)));
            return;
        }

        if (account.transferOwnership(newOwner.getReference().get().getUniqueId()).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(transferredOwnershipMessage));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
