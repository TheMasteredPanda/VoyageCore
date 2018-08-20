package cm.pvp.voyagepvp.voyagecore.features.vvoting.command;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.tasks.Tasks;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class VoteClaimCommand extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.messages.noclaimsavailable")
    private String noClaimsAvailableMessage;

    @ConfigPopulate("features.vvoting.messages.claimedreward")
    private String claimedRewardMessage;

    @ConfigPopulate("features.vvoting.messages.claims.gotmore")
    private String gotMoreClaims;

    @ConfigPopulate("features.vvoting.messages.claims.gotnone")
    private String gotNoClaims;

    public VoteClaimCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteclaim", "Claim your daily vote reward", true, "voteclaim");
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

        feature.getHandler().dailyClaims().whenCompleteAsync((claims, throwable) -> {
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }

            if (!claims.containsKey(p.getUniqueId())) {
                p.sendMessage(Format.colour(noClaimsAvailableMessage));
                return;
            }

            Tasks.runSyncLater(() -> feature.getDailyReward().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Format.format(cmd, "@p;" + p.getName()))), 0L);
            p.sendMessage(Format.colour(Format.format(claimedRewardMessage, "{claims?};" + (claims.get(p.getUniqueId()) > 0 ? Format.format(gotMoreClaims, "{amount};" + String.valueOf(claims.get(p.getUniqueId()))) : gotNoClaims))));
            feature.getHandler().updateDailyClaimCount(p.getUniqueId(), claims.get(p.getUniqueId()) - 1);
        });
    }
}
