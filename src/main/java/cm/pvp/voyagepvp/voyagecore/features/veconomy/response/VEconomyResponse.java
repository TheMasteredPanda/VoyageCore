package cm.pvp.voyagepvp.voyagecore.features.veconomy.response;

import com.google.common.collect.ImmutableMap;

public class VEconomyResponse
{
    private Action action;
    private Response response;
    private ImmutableMap<String, Object> values;

    private VEconomyResponse(Action action, Response response, ImmutableMap<String, Object> values)
    {
        this.action = action;
        this.response = response;
        this.values = values;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private Action action;
        private Response response;
        private ImmutableMap.Builder<String, Object> builder;

        private Builder()
        {
            builder = new ImmutableMap.Builder<>();
        }

        public Builder action(Action action)
        {
            this.action = action;
            return this;
        }

        public Builder response(Response response)
        {
            this.response = response;
            return this;
        }

        public Builder value(String key, Object value)
        {
            builder.put(key, value);
            return this;
        }

        public VEconomyResponse build()
        {
            return new VEconomyResponse(action, response, builder.build());
        }
    }
}
