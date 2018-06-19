package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class CustomPrefixCommand extends VoyageCommand
{
    public CustomPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "", "Parent command for regular player commands.", false, "cp", "customprefix");
        addChildren(new SetPrefixCommand(instance, feature), new ResetPrefixCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
