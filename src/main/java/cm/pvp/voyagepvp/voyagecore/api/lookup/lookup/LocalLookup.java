package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.lookup.Lookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.local.LocalStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;
import java.util.UUID;

public class LocalLookup implements Lookup, Listener
{
    private LocalStore store;
    private VoyageCore instance;
    private MojangLookup mojangLookup;

    public LocalLookup(LocalStore store, VoyageCore instance)
    {
        this.store = store;
        this.mojangLookup = instance.getMojangLookup();
        this.instance = instance;
    }

    @EventHandler
    public void on(PlayerJoinEvent e)
    {
        store.fetch(e.getPlayer().getUniqueId()).whenCompleteAsync((playerProfile, throwable) -> {
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }

            if (!playerProfile.isPresent()) {
                store.insert(new PlayerProfile(e.getPlayer().getName(), e.getPlayer().getUniqueId(), mojangLookup.lookup(e.getPlayer().getUniqueId()).get().getProperties()));
            }
        });
    }


    @Override
    public Optional<PlayerProfile> lookup(String name)
    {
        return Optional.empty();
    }

    @Override
    public Optional<PlayerProfile> lookup(UUID uuid)
    {
        return Optional.empty();
    }

}
