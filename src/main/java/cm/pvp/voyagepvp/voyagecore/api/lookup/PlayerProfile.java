package cm.pvp.voyagepvp.voyagecore.api.lookup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayerProfile
{
    @Setter
    private String name;

    private UUID id;

    @Setter
    private JsonArray properties;

    public PlayerProfile(JsonObject o)
    {
        name = o.get("name").getAsString();
        id = LookupUtil.formatUUID(o.get("id").getAsString());
        properties = o.getAsJsonArray("properties");
    }
}