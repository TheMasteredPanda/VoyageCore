package cm.pvp.voyagepvp.voyagecore.features.vvoting;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class VVotingPlaceholderExtension extends PlaceholderExpansion
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.voteparty.requiredvotes")
    private int requiredVotes;

    public VVotingPlaceholderExtension(VVoting feature)
    {
        this.feature = feature;

        try {
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getIdentifier()
    {
        return "voyagecore_vvoting";
    }

    @Override
    public String getAuthor()
    {
        return "TheMasteredPanda";
    }

    @Override
    public String getVersion()
    {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player p, String placeholder)
    {
        if (placeholder.equalsIgnoreCase("votepary_requiredvotes")) {
            return String.valueOf(requiredVotes);
        }

        if (placeholder.equalsIgnoreCase("voteparty_currentvotes")) {
            try {
                return String.valueOf(feature.getHandler().party().get().size());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (placeholder.equalsIgnoreCase("daily_claim_count")) {
            try {
                Map<UUID, Integer> dailyClaims = feature.getHandler().dailyClaims().get();

                return String.valueOf(dailyClaims.getOrDefault(p.getUniqueId(), 0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (placeholder.equalsIgnoreCase("voteparty_claim_count")) {
            try {
                Map<UUID, Integer> partyClaims = feature.getHandler().partyClaims().get();
                return String.valueOf(partyClaims.getOrDefault(p.getUniqueId(), 0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (placeholder.equalsIgnoreCase("votepary_countdown")) {
            return String.valueOf(feature.getCountdown().isCancelled() ? 0 : feature.getCountdown().getInterval());
        }

        return null;
    }
}
