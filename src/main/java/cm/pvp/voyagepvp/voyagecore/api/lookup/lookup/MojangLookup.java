package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup;

import cm.pvp.voyagepvp.voyagecore.api.lookup.Lookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.LookupUtil;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Gets the properties (skin, cape, name, and unique id) from Mojangs session servers.
 * In essence, this is meant to replace Bukkit#getOfflinePlayer.
 */
public class MojangLookup implements Lookup
{
    private final JsonParser parser = new JsonParser();
    private final String SESSION_SERVERS_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private Cache<String, UUID> namesCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private Cache<UUID, PlayerProfile> idCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    @Override
    public Optional<PlayerProfile> lookup(String name)
    {
        if (namesCache.asMap().containsKey(name)) {
            return lookup(namesCache.getIfPresent(name));
        }

        return lookup(LookupUtil.getUniqueId(name));
    }

    @Override
    public Optional<PlayerProfile> lookup(UUID uuid)
    {
        if (idCache.asMap().containsKey(uuid)) {
            return Optional.of(idCache.getIfPresent(uuid));
        }


        try {
            URL url = new URL(SESSION_SERVERS_URL + uuid.toString().replace("-", ""));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            int response = connection.getResponseCode();

            if (response != HttpsURLConnection.HTTP_OK) {
                return Optional.empty();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder context = new StringBuilder();
            String input;


            while ((input = in.readLine()) != null) {
                context.append(input);
            }

            PlayerProfile profile = new PlayerProfile(parser.parse(context.toString()).getAsJsonObject());
            idCache.put(profile.getId(), profile);
            namesCache.put(profile.getName(), profile.getId());
            return Optional.of(profile);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
