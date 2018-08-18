package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.lookup.Lookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;

import java.util.Optional;
import java.util.UUID;

public class BackupLookup implements Lookup
{
    private VoyageCore instance;
    private MojangLookup mojangLookup;
    private LocalLookup localLookup;

    public BackupLookup(VoyageCore instance)
    {
        this.instance = instance;
        mojangLookup = instance.getMojangLookup();
        localLookup = instance.getLocalLookup();
    }


    @Override
    public Optional<PlayerProfile> lookup(String name)
    {
        if (localLookup.lookup(name).isPresent()) {
            return localLookup.lookup(name);
        } else {
            return mojangLookup.lookup(name);
        }
    }

    @Override
    public Optional<PlayerProfile> lookup(UUID uuid)
    {
        if (localLookup.lookup(uuid).isPresent()) {
            return localLookup.lookup(uuid);
        } else {
            return mojangLookup.lookup(uuid);
        }
    }
}
