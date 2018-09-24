package cm.pvp.voyagepvp.voyagecore.features.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.announcement.AnnouncementThread;
import cm.pvp.voyagepvp.voyagecore.features.announcement.Announcements;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class StartAnnouncementThread extends VoyageCommand
{
    private Announcements feature = null;
    private VoyageCore instance;

    @ConfigPopulate("features.announcements.messages.announcementthreadrunning")
    private String announcementThreadRunning;

    @ConfigPopulate("features.announcements.messages.announcementthreadstarted")
    private String announcementThreadStarted;

    public StartAnnouncementThread(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.command.start", "Start announcements automatically broadcasting.",false, "start");
        this.instance = instance;
        this.feature = feature;

        instance.getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (feature.getAnnouncementThread() != null) {
            sender.sendMessage(Format.colour(Format.format(announcementThreadRunning, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
            return;
        }

        feature.setAnnouncementThread(new AnnouncementThread(instance, feature.getAnnouncements(), feature.getSection().getBoolean("random"), feature.getSection().getInt("interval") * 20));
        sender.sendMessage(Format.colour(Format.format(announcementThreadStarted, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
        return;
    }
}
