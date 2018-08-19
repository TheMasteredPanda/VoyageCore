package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class CountCommand extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.messages.voteparty.votecount")
    private String voteCountMessage;

    @ConfigPopulate("features.vvoting.requiredVotes")
    private String requiredVotes;

    public CountCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteparty.count", "Displays the amount of votes that have contributed to the party, and the amount required for the players of that party to receive a reward.", true, "count");
        this.feature = feature;

        try {
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        feature.getHandler().party().whenCompleteAsync((uuids, throwable) -> sender.sendMessage(Format.colour(Format.format(voteCountMessage, "{count};" + String.valueOf(uuids.size()), "{required};" + requiredVotes))));
    }
}
