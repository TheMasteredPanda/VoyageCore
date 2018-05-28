package cm.pvp.voyagepvp.voyagecore.api.lookup.lookup;

import cm.pvp.voyagepvp.voyagecore.api.exception.HTTPException;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Gets the properties (skin, cape, name, and unique id) from Mojangs session servers.
 * In essence, this is meant to replace Bukkit#getOfflinePlayer.
 */
public class MojangLookup implements Lookup
{
    private final JsonParser parser = new JsonParser();
    private Cache<UUID, PlayerProfile> idCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private Cache<String, PlayerProfile> nameCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    private final String SESSION_SERVERS_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    @Override
    public PlayerProfile lookup(String name)
    {
        if (nameCache.asMap().containsKey(name)) {
            return nameCache.getIfPresent(name);
        }

        PlayerProfile playerProfile = lookup(LookupUtil.getUniqueId(name));
        nameCache.put(playerProfile.getName(), playerProfile);
        idCache.put(playerProfile.getId(), playerProfile);
        return playerProfile;
    }

    @Override
    public PlayerProfile lookup(UUID uuid)
    {
        if (idCache.asMap().containsKey(uuid)) {
            return idCache.getIfPresent(uuid);
        }

        try {
            URL url = new URL(SESSION_SERVERS_URL + uuid.toString().replace("-", ""));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            int response = connection.getResponseCode();

            if (response != HttpsURLConnection.HTTP_OK) {
                throw new HTTPException("Response was not 200 when attempting to get properties of player with id " + uuid.toString() + ".");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder context = new StringBuilder();
            String input;


            while ((input = in.readLine()) != null) {
                context.append(input);
            }

            PlayerProfile playerProfile = new PlayerProfile(parser.parse(context.toString()).getAsJsonObject());
            nameCache.put(playerProfile.getName(), playerProfile);
            idCache.put(playerProfile.getId(), playerProfile);
            return playerProfile;
        } catch (IOException e) {
            throw new MojangException(e);
        }
    }
}
