package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.local;

import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LocalStore
{
    void insert(PlayerProfile playerProfile);

    void update(PlayerProfile playerProfile);

    void delete(PlayerProfile playerProfile);

    CompletableFuture<Optional<PlayerProfile>> fetch(String name);

    CompletableFuture<Optional<PlayerProfile>> fetch(UUID uuid);

    Optional<List<PlayerProfile>> fetch(String[] names);

    Optional<List<PlayerProfile>> fetch(UUID[] uuids);

    CompletableFuture<Date> getLastUpdated(UUID id);

    String id();
}
