package cm.pvp.voyagepvp.voyagecore.modules.announcement.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.Command;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.Announcements;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class ListAnnouncements extends Command
{
    public ListAnnouncements(VoyageCore instance, Announcements feature)
    {
        super(null, "voyagecore.announcements.command.list", "List the announcements configured", true, "list");
    }

    @Override
    public void execute(CommandSender sender, Command command, LinkedList<String> arguments)
    {

    }
}
