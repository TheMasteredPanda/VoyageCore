package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class AdminSetPrefixCommand extends VoyageCommand
{
    public AdminSetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.admin.set", "Set a players prefix", false, "aset");

        try {
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {

    }
}
