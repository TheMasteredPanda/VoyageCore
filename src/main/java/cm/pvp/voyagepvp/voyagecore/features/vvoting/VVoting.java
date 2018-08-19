package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.TestVoteCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.VotePartyCommand;
import com.vexsoftware.votifier.model.VotifierEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

@Getter
public class VVoting extends Feature implements Listener
{
    private DataHandler handler;

    @ConfigPopulate("features.vvoting.messages.vote")
    private String voteMessage;

    @ConfigPopulate("features.vvoting.messages.voteparty.claimsavailable")
    private String votePartyClaimsAvailable;

    @ConfigPopulate("features.vvoting.messages.thanksforvoting")
    private String thanksForVotingMessage;

    @ConfigPopulate("features.vvoting.dailyreward")
    private List<String> dailyReward;

    public VVoting(VoyageCore instance)
    {
        super(instance, "VVoting", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        handler = new DataHandler(this);
        getInstance().register(new VotePartyCommand(this), new TestVoteCommand());
        return true;
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        handler.voted(e.getPlayer().getUniqueId()).whenCompleteAsync((voted, throwable) -> {
            if (!voted) {
                e.getPlayer().sendMessage(Format.colour(voteMessage));
            }

            handler.claims().whenCompleteAsync((claims, throwable1) -> {
                if (!claims.containsKey(e.getPlayer().getUniqueId())) {
                    return;
                }

                e.getPlayer().sendMessage(Format.colour(Format.format(votePartyClaimsAvailable, "{amount};" + String.valueOf(claims.get(e.getPlayer().getUniqueId())))));
            });
        });
    }

    @EventHandler
    public void on(VotifierEvent e)
    {
        if (Players.online().stream().anyMatch(p -> p.getName().equalsIgnoreCase(e.getVote().getUsername()))) {
            Player p = Bukkit.getPlayer(e.getVote().getUsername());

            dailyReward.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            p.sendMessage(Format.colour(Format.format(thanksForVotingMessage, "{player};" + p.getName(), "{servicename};" + e.getVote().getServiceName())));
        }
    }
}
