package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.api.player.PlayerWrapper;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.PlayerAccount;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class VEconomyPlayer extends PlayerWrapper
{
    @Getter
    private PlayerAccount account;

    public VEconomyPlayer(Player p, PlayerAccount account, ArrayList<UUID> sharedAccounts)
    {
        super(p);
        this.account = account;
    }
}
