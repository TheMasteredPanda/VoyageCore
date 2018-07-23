package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BankMembershipRequestDeclineCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.norequestfromspecifiedbank")
    private String noRequestFromSpecifiedBankMessage;

    @ConfigPopulate("features.veconomy.messages.bank.membershiprequestdeclined")
    private String membershipRequestDeclinedMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    public BankMembershipRequestDeclineCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.declinerequest", "Decline a bank membership request.", true, "decline");
        this.feature = feature;

        try {
            addArguments(new ArgumentField("bank name", true));
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer player = feature.get(p);

        MembershipRequest req = player.getMembershipRequests().stream().filter(r -> feature.getHandler().sharedAccountExists(r.getAccountId()) && feature.getAccount(r.getAccountId()).getName().equalsIgnoreCase(arguments.get(0))).findFirst().orElse(null);

        if (req == null) {
            p.sendMessage(Format.colour(noRequestFromSpecifiedBankMessage));
            return;
        }

        if (player.removeRequest(req.getAccountId()).getResponse() == Response.SUCCESS) {
            p.sendMessage(Format.colour(Format.format(membershipRequestDeclinedMessage, "{bank};" + arguments.get(0))));
        } else {
            p.sendMessage(Format.colour(errorMessage));
        }
    }
}
