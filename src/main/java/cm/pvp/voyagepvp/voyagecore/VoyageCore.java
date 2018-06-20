package cm.pvp.voyagepvp.voyagecore;

import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.Config;
import cm.pvp.voyagepvp.voyagecore.api.lookup.lookup.MojangLookup;
import cm.pvp.voyagepvp.voyagecore.api.module.ModuleManager;
import cm.pvp.voyagepvp.voyagecore.api.plugin.VoyagePlugin;
import cm.pvp.voyagepvp.voyagecore.features.announcement.Announcements;
import cm.pvp.voyagepvp.voyagecore.features.chatreaction.ChatReaction;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import cm.pvp.voyagepvp.voyagecore.features.mention.Mention;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;

@Getter
public class VoyageCore extends VoyagePlugin
{
    private static VoyageCore instance;
    private MojangLookup mojangLookup;
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
                new Mention(this)
        );
    }

    @Override
    public void onEnable()
    {
        bootAll();
    }
}
