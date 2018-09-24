package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ImmutableMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.LinkedList;

public class BankMembershipRequestAcceptCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.norequestfromspecifiedbank")
    private String noRequestFromSpecifiedBankMessage;

    @ConfigPopulate("features.veconomy.messages.bank.membershiprequestaccepted")
    private String membershipRequestAcceptedMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    public BankMembershipRequestAcceptCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.acceptrequest", "Accept a bank membership request.", true, "accept");
        this.feature = feature;

        addArguments(new ArgumentField("bank name", true));
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer player = feature.get(p);

        MembershipRequest req = player.getMembershipRequests().stream().filter(r -> feature.getHandler().sharedAccountExists(r.getAccountId()) && feature.getAccount(r.getAccountId()).getName().equalsIgnoreCase(arguments.get(0))).findFirst().orElse(null);

        if (req == null)  {
            p.sendMessage(Format.colour(noRequestFromSpecifiedBankMessage));
            return;
        }

        SharedAccount account = feature.getAccount(req.getAccountId());

        if (account.addMember(p.getUniqueId(), SharedAccount.Type.MEMBER).getResponse() == Response.SUCCESS && player.removeRequest(req.getAccountId()).getResponse() == Response.SUCCESS) {
            feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), p.getUniqueId(), Action.MEMBERSHIP_ACCEPTED, new Date(), ImmutableMap.<String, Object>builder().put("requester", req.getRequester()).build()));
            player.getSharedAccounts().add(account.getId());
            p.sendMessage(Format.colour(Format.format(membershipRequestAcceptedMessage, "{bank};" + account.getName())));
        } else {
            p.sendMessage(Format.colour(errorMessage));
        }
    }
}
