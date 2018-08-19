package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.BackupLookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.LocalLookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.local.DefaultStore;
import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import cm.pvp.voyagepvp.voyagecore.features.announcement.Announcements;
import cm.pvp.voyagepvp.voyagecore.features.chatreaction.ChatReaction;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import cm.pvp.voyagepvp.voyagecore.features.mention.Mention;
import cm.pvp.voyagepvp.voyagecore.features.norain.NoRain;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Main class.
 */
@Getter
public class VoyageCore extends VoyagePlugin
{
    private static VoyageCore instance;
    private MojangLookup mojangLookup;
    private LocalLookup localLookup;
    private BackupLookup backupLookup;
    private Config<VoyageCore> mainConfig;

    public VoyageCore()
    {
        instance = this;
    }

    public static VoyageCore get()
    {
        return instance;
    }

    public MojangLookup getMojangLookup()
    {
        if (mojangLookup == null) {
            mojangLookup = new MojangLookup();
        }

        return mojangLookup;
    }

    public LocalLookup getLocalLookup()
    {
        if (localLookup == null) {
            localLookup = new LocalLookup(new DefaultStore(
                    mainConfig.raw().getString("core.locallookup.mariadb.username"), mainConfig.raw().getString("core.locallookup.mariadb.password"),
                    mainConfig.raw().getString("core.locallookup.mariadb.database"), mainConfig.raw().getString("core.locallookup.mariadb.host")), this);
        }

        return localLookup;
    }

    public BackupLookup getBackupLookup()
    {
        if (backupLookup == null) {
            backupLookup = new BackupLookup(this);
        }

        return backupLookup;
    }


    @Override
    public void onLoad()
    {
        try {
            mainConfig = new Config<>(this, new File(getDataFolder() + File.separator + "config.yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        add(new ModuleManager(this));
        get(ModuleManager.class).add(this,
                new Announcements(this),
                new ChatReaction(this),
                new CustomPrefix(this),
                new Mention(this),
                new VEconomy(this),
                new NoRain(this),
                new VVoting(this)
        );
    }

    @Override
    public void onEnable()
    {
        bootAll();
    }
}
