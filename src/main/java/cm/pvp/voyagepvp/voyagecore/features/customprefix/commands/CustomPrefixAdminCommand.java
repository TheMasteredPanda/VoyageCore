package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class CustomPrefixAdminCommand extends VoyageCommand
{
    public CustomPrefixAdminCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "", "Parent command for CustomPrefix admin commands.", false, "cpa", "customprefixadmin");
        addChildren(new AdminResetPrefixCommand(instance, feature), new AdminSetPrefixCommand(instance, feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
