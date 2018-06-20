package cm.pvp.voyagepvp.voyagecore.features.customprefix;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.CustomPrefixAdminCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.commands.CustomPrefixCommand;
import lombok.Getter;
import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;


//TODO add blacklisted words.
@Getter
public class CustomPrefix extends Feature
{
    private LuckPermsApi api;

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

        getInstance().register(new CustomPrefixCommand(getInstance(), this), new CustomPrefixAdminCommand(getInstance(), this));
        return true;
    }
}
