package cm.pvp.voyagepvp.voyagecore.api.player;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;

/**
 * Wrapper template for player wrappers.
 */
public class PlayerWrapper
{
    @Getter
    private WeakReference<Player> reference;

    public PlayerWrapper(Player player)
    {
        reference = new WeakReference<>(player);
    }
}
