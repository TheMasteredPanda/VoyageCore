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
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.playerismember")
    private String targetIsAlreadyMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.addedplayer")
    private String addedMemberMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public AddMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.addmember", "Add a member to a bank.", false, "add");
        this.feature = feature;

        ArgumentField ownerArg = new ArgumentField("owner's name (player name)", true);
        ownerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));
        ArgumentField memberArg = new ArgumentField("player name", true);

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

        VEconomyPlayer newMember = feature.get(lookup.lookup(arguments.get(2)).get().getId());

        if (account.isMember(newMember.getReference().get().getUniqueId())) {
            sender.sendMessage(Format.colour(Format.format(targetIsAlreadyMemberMessage, "{target};" + arguments.get(2))));
            return;
        }

        if (account.addMember(newMember.getReference().get().getUniqueId(), SharedAccount.Type.MEMBER).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(addedMemberMessage, "{target};" + arguments.get(2))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
