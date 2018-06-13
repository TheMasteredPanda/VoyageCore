package cm.pvp.voyagepvp.voyagecore.modules.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.Command;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.Announcements;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class StopAnnouncementThread extends Command
{
    private Announcements feature = null;

    public StopAnnouncementThread(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.command.stop", "Stop the announcements from automatically broadcasting.", false, "stop");
    }

    @Override
    public void execute(CommandSender sender, Command command, LinkedList<String> arguments)
    {

    }
}
