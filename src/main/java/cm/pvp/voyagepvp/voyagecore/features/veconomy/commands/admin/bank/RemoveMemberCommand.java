package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.VEconomyResponse;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RemoveMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String targetIsNotMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisowner")
    private String targetIsOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.removedmember")
    private String removedMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public RemoveMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.removemember", "Remove a member from a bank", false, "remove");
        this.feature = feature;

        ArgumentField ownerArg = new ArgumentField("owner's name (player name)", true);
        ownerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));
        ArgumentField memberArg = new ArgumentField("member's name (player name)", true);
        memberArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(ownerArg, new ArgumentField("bank name", true), memberArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        MojangLookup lookup = feature.getInstance().getMojangLookup();
        VEconomyPlayer owner = feature.get(lookup.lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = owner.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(owner.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1))));
            return;
        }

        SharedAccount account = ownedBanks.stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).map(id -> feature.getAccount(id)).findFirst().get();
        UUID memberId = lookup.lookup(arguments.get(2)).get().getId();

        if (!account.isMember(memberId)) {
            sender.sendMessage(Format.colour(Format.format(targetIsNotMemberMessage, "{target};" + arguments.get(2))));
            return;
        }

        if (account.getOwner().equals(memberId)) {
            sender.sendMessage(Format.colour(targetIsOwnerMessage));
            return;
        }


        VEconomyResponse response = account.removeMember(memberId, account.getMembers().get(memberId));

        if (response.getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(removedMemberMessage, "{target};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage + "// " + response.getValues().get("message")));
        }
    }
}
