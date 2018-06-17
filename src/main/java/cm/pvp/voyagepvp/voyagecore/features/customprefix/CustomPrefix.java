package cm.pvp.voyagepvp.voyagecore.features.customprefix;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.ResetPrefixCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.SetPrefixCommand;
import lombok.Getter;
import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

@Getter
public class CustomPrefix extends Feature
{
    private LuckPermsApi api;
    private DataHandler handler;

    public CustomPrefix(VoyageCore instance)
    {
        super(instance, "CustomPrefix", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        RegisteredServiceProvider<LuckPermsApi> provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);

        if (provider != null) {
            api = provider.getProvider();
        }

        handler = new DataHandler(getInstance());

        getInstance().register(new SetPrefixCommand(getInstance(), this), new ResetPrefixCommand(this));
        return true;
    }
}
