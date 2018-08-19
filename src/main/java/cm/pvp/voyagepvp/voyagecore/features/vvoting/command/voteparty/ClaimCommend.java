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

    @ConfigPopulate("features.vvoting.voteparty.reward")
    private List<String> commands;

    @ConfigPopulate("features.vvoting.messages.noclaimsavailable")
    private String noClaimsAvailableMessage;

    @ConfigPopulate("features.vvoting.messages.claimedreward")
    private String claimedRewardMessage;

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

        feature.getHandler().claims().whenCompleteAsync((claims, throwable) -> {
            if (!claims.containsKey(p.getUniqueId())) {
                p.sendMessage(Format.colour(noClaimsAvailableMessage));
                return;
            }

            commands.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            p.sendMessage(Format.colour(Format.format(claimedRewardMessage, "{claimsleft};" + String.valueOf(claims.get(p.getUniqueId())))));
            feature.getHandler().updateClaimCount(p.getUniqueId(), claims.get(p.getUniqueId()) - 1);
        });
    }
}
