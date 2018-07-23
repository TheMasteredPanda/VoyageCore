package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BankListInvitesCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.listRequests.informationTemplate")
    private List<String> requestInformationTemplate;

    @ConfigPopulate("features.veconomy.messages.bank.listRequests.header")
    private String listRequestsHeader;

    @ConfigPopulate("features.veconomy.messages.bank.listRequests.footer")
    private String listRequestsFooter;

    @ConfigPopulate("features.veconomy.messages.bank.nomembershiprequests")
    private String noMembershipRequestsMessage;

    @ConfigPopulate("features.veconomy.messages.bank.listRequests.entry")
    private String requestListEntry;

    public BankListInvitesCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.listinvites", "List all membership invitations to banks.", true, "invites");
        this.feature = feature;

        try {
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

        if (player.getMembershipRequests().size() == 0) {
            p.sendMessage(Format.colour(noMembershipRequestsMessage));
            return;
        }

        TextComponent message = new TextComponent();
        message.addExtra(Format.colour(listRequestsHeader) + "\n");

        for (Iterator<MembershipRequest> iterator = player.getMembershipRequests().iterator(); iterator.hasNext(); ) {
            MembershipRequest req = iterator.next();
            List<String> desc = requestInformationTemplate;

            if (!feature.getHandler().sharedAccountExists(req.getAccountId())) {
                iterator.remove();
                continue;
            }

            SharedAccount account = feature.getAccount(req.getAccountId());

            MojangLookup lookup = feature.getInstance().getMojangLookup();

            Preconditions.checkNotNull(req);
            Preconditions.checkNotNull(req.getRequester());

            String name = lookup.lookup(req.getRequester()).get().getName();
            String bankOwnerName = lookup.lookup(account.getOwner()).get().getName();
            desc.replaceAll(s -> Format.colour(Format.format(s, "{date};" + req.getDate().toString(), "{bankname};" + account.getName(), "{bankowner};" + bankOwnerName)));
            TextComponent extra = new TextComponent(Format.colour(Format.format(requestListEntry, "{name};" + name)));
            extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Joiner.on("\n").join(desc))));
            message.addExtra(extra);

            if (iterator.hasNext()) {
                message.addExtra("\n");
            }
        }

        message.addExtra(Format.colour(listRequestsFooter));
        p.spigot().sendMessage(message);
    }
}
