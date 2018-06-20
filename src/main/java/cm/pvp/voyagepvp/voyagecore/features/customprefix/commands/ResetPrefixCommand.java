package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class ResetPrefixCommand extends VoyageCommand
{
    private CustomPrefix feature;

    @ConfigPopulate("features.customprefix.messages.resetprefix")
    private String resetPrefix;

    public ResetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.reset", "Remove the custom prefix", true, "reset");
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta removeprefix 9000");
        sender.sendMessage(Format.colour(resetPrefix));
    }
}
