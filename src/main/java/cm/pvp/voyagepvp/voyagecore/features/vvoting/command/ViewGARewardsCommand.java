package cm.pvp.voyagepvp.voyagecore.features.vvoting.command;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class ViewGARewardsCommand extends VoyageCommand
{
    @ConfigPopulate("features.vvoting.messages.garewardslist")
    private List<String> gaRewardsListMessage;


    public ViewGARewardsCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.gawardslist", "Presents a list of rewards given by GA in VVoting", true, "garewards");

        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        gaRewardsListMessage.forEach(line -> sender.sendMessage(Format.colour(line)));
    }
}
