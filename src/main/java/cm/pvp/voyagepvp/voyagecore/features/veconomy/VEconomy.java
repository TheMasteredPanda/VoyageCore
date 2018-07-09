package cm.pvp.voyagepvp.voyagecore.features.veconomy;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.VEconomyCommand;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VEconomy extends Feature implements Listener
{
    private Cache<UUID, VEconomyPlayer> players = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private Cache<UUID, SharedAccount> sharedAccounts = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    @Getter
    private DataHandler handler;

    public VEconomy(VoyageCore instance)
    {
        super(instance, "VEconomy", 1.0);
        handler = new DataHandler(this);
    }

    @Override
    protected boolean enable() throws Exception
    {
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        getInstance().register(new VEconomyCommand(this));
        return true;
    }

    public VEconomyPlayer get(Player player)
    {
        if (players.asMap().containsKey(player.getUniqueId())) {
            return players.getIfPresent(player.getUniqueId());
        }

        VEconomyPlayer ePlayer = handler.getPlayer(player);
        players.put(player.getUniqueId(), ePlayer);
        return ePlayer;
    }

    public VEconomyPlayer get(UUID id)
    {
        if (players.asMap().containsKey(id)) {
            return players.getIfPresent(id);
        }

        Player target = Bukkit.getPlayer(id);

        if (target == null) {
            return null;
        }

        return get(target);
    }

    public SharedAccount getAccount(UUID id)
    {
        if (sharedAccounts.asMap().containsKey(id)) {
            return sharedAccounts.getIfPresent(id);
        }

        SharedAccount account = handler.getSharedAccount(id);
        sharedAccounts.put(id, account);
        return account;
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        if (players.asMap().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }

        if (handler.playerExists(e.getPlayer().getUniqueId())) {
            players.put(e.getPlayer().getUniqueId(), handler.createPlayer(e.getPlayer()));
        } else {
            players.put(e.getPlayer().getUniqueId(), handler.getPlayer(e.getPlayer()));
        }
    }

    public SharedAccount createSharedAccount(UUID owner, String name)
    {
        SharedAccount account = handler.createSharedAccount(owner, name);
        sharedAccounts.put(account.getId(), account);
        get(owner).getSharedAccounts().add(account.getId());
        return account;
    }

    public void removeSharedAccount(UUID accountId)
    {
        if (sharedAccounts.asMap().containsKey(accountId)) {
            getAccount(accountId).getMembers().keySet().forEach(id -> get(id).getSharedAccounts().remove(accountId));
            sharedAccounts.invalidate(accountId);
        }

        handler.removedSharedAccount(accountId);
    }
}
