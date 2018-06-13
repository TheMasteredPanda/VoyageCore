package cm.pvp.voyagepvp.voyagecore.modules.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.Command;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.config.ConfigManager;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.AnnouncementThread;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.Announcements;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class StartAnnouncementThread extends Command
{
    private Announcements feature = null;
    private VoyageCore instance;

    @ConfigPopulate("modules.announcements.messages.AnnouncementThreadRunning")
    private String announcementThreadRunning;

    @ConfigPopulate("modules.announcements.messages.AnnouncementThreadStarted")
    private String announcementThreadStarted;

    public StartAnnouncementThread(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.command.start", "Start announcements automatically broadcasting.",false, "start");
        this.instance = instance;

        try {
            instance.get(ConfigManager.class).get("mainConfig").populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, Command command, LinkedList<String> arguments)
    {
        if (feature.getAnnouncementThread() != null) {
            sender.sendMessage(Format.colour(Format.format(announcementThreadRunning, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
            return;
        }

        feature.setAnnouncementThread(new AnnouncementThread(instance, feature.getAnnouncements(), feature.getSection().getBoolean("random"), feature.getSection().getInt("interval")));
        sender.sendMessage(Format.colour(Format.format(announcementThreadStarted, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
    }
}
