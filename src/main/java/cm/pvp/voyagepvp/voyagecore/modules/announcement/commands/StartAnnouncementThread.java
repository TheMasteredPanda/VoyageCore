package cm.pvp.voyagepvp.voyagecore.modules.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.Command;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.Announcements;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class StartAnnouncementThread extends Command
{
    private Announcements feature = null;

    public StartAnnouncementThread(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.command.start", "Start announcements automatically broadcasting.",false, "start");
    }

    @Override
    public void execute(CommandSender sender, Command command, LinkedList<String> arguments)
    {

    }
}
