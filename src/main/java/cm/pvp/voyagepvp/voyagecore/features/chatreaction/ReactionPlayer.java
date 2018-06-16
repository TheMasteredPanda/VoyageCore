package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.api.player.PlayerWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class ReactionPlayer extends PlayerWrapper
{
    private int wins;
    private long fastest;

    public ReactionPlayer(Player player, int wins, long fastest)
    {
        super(player);
        this.wins = wins;
        this.fastest = fastest;
    }
}
