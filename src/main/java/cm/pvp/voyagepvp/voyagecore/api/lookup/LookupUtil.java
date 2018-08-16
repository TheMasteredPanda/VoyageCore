package cm.pvp.voyagepvp.voyagecore.api.lookup;

import cm.pvp.voyagepvp.voyagecore.api.exception.HTTPException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of methods to assist with lookup instances.
 */
public final class LookupUtil
{
    private static final JsonParser PARSER = new JsonParser();
    private static final String API_MOJANG_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern DASHLESS_PATTERN = Pattern.compile("^([A-Fa-f0-9]{8})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{4})([A-Fa-f0-9]{12})$");

    public static UUID getUniqueId(String name) throws HTTPException
    {
        try {
            URL url = new URL(API_MOJANG_URL + name);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");

            int response = connection.getResponseCode();

            if (response != HttpsURLConnection.HTTP_OK) {
                throw new HTTPException("Attempted to retrieve player " + name + "s unique id. However, we didn't get the response code 200." +
                        " Response code: " + response + " using method GET on " + API_MOJANG_URL + name + ".");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder context = new StringBuilder();

            String input;

            while((input = in.readLine()) != null) {
                context.append(input);
            }

            JsonObject json = PARSER.parse(context.toString()).getAsJsonObject();

            Matcher matcher = DASHLESS_PATTERN.matcher(json.get("id").getAsString());

            if (!matcher.matches()) {
                throw new RuntimeException("Couldn't parse id " + json.get("id").getAsString() + " as it didn't match the regex.");
            }

            return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new NullPointerException("Couldn't find unique id for " + name + ".");
    }

    public static UUID formatUUID(String s)
    {
        Matcher matcher = DASHLESS_PATTERN.matcher(s);

        if (!matcher.matches()) {
            throw new RuntimeException("Can't parse " + s + ", it isn't an unparsed uuid.");
        }

        return UUID.fromString(matcher.replaceAll("$1-$2-$3-$4-$5"));
    }
}
