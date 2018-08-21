package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;

public class ClaimCommend extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.voteparty.rewards")
    private List<String> rewards;

    @ConfigPopulate("features.vvoting.messages.noclaimsavailable")
    private String noClaimsAvailableMessage;

    @ConfigPopulate("features.vvoting.messages.claimedreward")
    private String claimedRewardMessage;

    @ConfigPopulate("features.vvoting.messages.claims.gotmore")
    private String gotMoreClaims;

    @ConfigPopulate("features.vvoting.messages.claims.gotnone")
    private String gotNoClaims;

    public ClaimCommend(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteparty.claim", "Claim a reward you have received from contributing to a vote party.", true, "claim");
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

        feature.getHandler().partyClaims().whenCompleteAsync((claims, throwable) -> {
            if (!claims.containsKey(p.getUniqueId())) {
                p.sendMessage(Format.colour(noClaimsAvailableMessage));
                return;
            }

            rewards.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            p.sendMessage(Format.colour(Format.format(claimedRewardMessage, "{claims?};" + (claims.get(p.getUniqueId()) > 0 ? Format.format(gotMoreClaims, "{amount};" + String.valueOf(claims.get(p.getUniqueId()))) : gotNoClaims))));
            feature.getHandler().updatePartyClaimCount(p.getUniqueId(), claims.get(p.getUniqueId()) - 1);
        });
    }
}
