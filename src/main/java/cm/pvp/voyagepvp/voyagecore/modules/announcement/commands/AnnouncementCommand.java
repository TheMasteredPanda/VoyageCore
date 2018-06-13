package cm.pvp.voyagepvp.voyagecore.modules.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.Command;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.Announcements;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class AnnouncementCommand extends Command
{
    public AnnouncementCommand(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcement.commands", "Parent command for the announcement feature.", false, "announcement");
        addChildren(new StopAnnouncementThread(instance, feature));
    }

    @Override
    public void execute(CommandSender sender, Command command, LinkedList<String> arguments)
    {

    }
}
