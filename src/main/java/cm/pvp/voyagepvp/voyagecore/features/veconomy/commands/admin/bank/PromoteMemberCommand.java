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

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PromoteMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String targetIsNotMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerispoa")
    private String targetIsaPOAMessage;

    @ConfigPopulate("features.veconomy.messages.bank.promotedplayer")
    private String promotedMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;


    public PromoteMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.promote", "Promote a member to POA in a bank.", false, "promote");
        this.feature = feature;

        ArgumentField ownerArg = new ArgumentField("owner's name (player name)", true);
        ownerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));
        ArgumentField memberArg = new ArgumentField("member's name (player name)", true);
        memberArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));

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
        Lookup lookup = feature.getInstance().getBackupLookup();
        VEconomyPlayer player = feature.get(lookup.lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(player.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        SharedAccount account = ownedBanks.stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(1))).map(id -> feature.getAccount(id)).findFirst().get();
        UUID memberId = lookup.lookup(arguments.get(2)).get().getId();

        if (!account.isMember(memberId)) {
            sender.sendMessage(Format.colour(Format.format(targetIsNotMemberMessage, "{target};" + arguments.get(2))));
            return;
        }

        if (account.getMembers().get(memberId) == SharedAccount.Type.POA) {
            sender.sendMessage(Format.colour(targetIsaPOAMessage));
            return;
        }

        if (account.promoteMember(memberId).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(promotedMemberMessage, "{target};" + arguments.get(2))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
