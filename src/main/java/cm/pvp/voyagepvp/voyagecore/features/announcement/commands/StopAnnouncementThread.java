package cm.pvp.voyagepvp.voyagecore.features.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.announcement.Announcements;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class StopAnnouncementThread extends VoyageCommand
{
    private Announcements feature;

    @ConfigPopulate("features.announcements.messages.announcementthreadnotrunning")
    private String announcementThreadNotRunning;

    @ConfigPopulate("features.announcements.messages.announcementthreadstopped")
    private String announcementThreadStopped;

    public StopAnnouncementThread(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.command.stop", "Stop the announcements from automatically broadcasting.", false, "stop");
        this.feature = feature;

        try {
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (feature.getAnnouncementThread() == null) {
            sender.sendMessage(Format.colour(Format.format(announcementThreadNotRunning, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
            return;
        }

        feature.getAnnouncementThread().cancel();
        feature.setAnnouncementThread(null);
        sender.sendMessage(Format.colour(Format.format(announcementThreadStopped, "{commandprefix};" + getLocale().get(CommandLocale.Key.COMMAND_PREFIX))));
        return;
    }
}
