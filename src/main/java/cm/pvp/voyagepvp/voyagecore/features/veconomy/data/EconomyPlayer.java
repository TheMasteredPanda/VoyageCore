package cm.pvp.voyagepvp.voyagecore.features.veconomy.data;

import cm.pvp.voyagepvp.voyagecore.api.player.PlayerWrapper;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class EconomyPlayer extends PlayerWrapper
{
    private VEconomy feature;
    private BigDecimal balance;

    private ArrayList<UUID> accessibleBanks = Lists.newArrayList();

    public EconomyPlayer(VEconomy feature, Player player, BigDecimal balance)
    {
        super(player);
        this.feature = feature;
        this.balance = balance;
    }

    public boolean isAccessible(String bankName)
    {

    }

    public void addBankReference(UUID ref)

    {

    }

    public void removeBankReference(UUID ref)
    {

    }
}

