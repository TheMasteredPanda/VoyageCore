package cm.pvp.voyagepvp.voyagecore.api.lookup;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for databases or services holding information
 * on players properties.
 */
public interface Lookup
{
    /**
     * Look up a player by their name.
     * @param name - the players name.
     * @return the players profile.
     */
    Optional<PlayerProfile> lookup(String name);

    /**
     * Lookup a player by their unique id.
     * @param uuid - the players unique id.
     * @return the players profile.
     */
    Optional<PlayerProfile> lookup(UUID uuid);
}
