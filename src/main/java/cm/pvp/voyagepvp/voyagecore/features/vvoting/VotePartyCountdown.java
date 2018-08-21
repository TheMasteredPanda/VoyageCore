package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

public class VotePartyCountdown extends BukkitRunnable
{
    private VVoting feature;

    @Getter
    private int interval;

    public VotePartyCountdown(VVoting feature, int interval)
    {
        this.feature = feature;
        this.interval = interval;
    }

    @Override
    public void run()
    {
        interval--;

        if (interval <= 0) {
            feature.startVotingParty();
        }
    }
}
