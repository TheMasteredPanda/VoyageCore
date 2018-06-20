package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class AdminResetPrefixCommand extends VoyageCommand
{
    private VoyageCore instance;
    private CustomPrefix feature;

    @ConfigPopulate("features.customprefix.messages.resetprefix")
    private String resetPrefix;

    @ConfigPopulate("features.customprefix.messages.noprefixfound")
    private String noPrefixFound;

    public AdminResetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.admin.reset", "Reset a players prefix.", false, "reset");
        this.instance = instance;
        this.feature = feature;

        try {
            ArgumentField field = new ArgumentField("player name", true);
            field.setCheckFunction(new PlayerCheckFunction(instance.getMojangLookup()));
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta removeprefix 9000");
        sender.sendMessage(Format.colour(Format.format(resetPrefix)));
    }
}
