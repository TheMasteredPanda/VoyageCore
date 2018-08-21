package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class VotePartyCommand extends VoyageCommand
{
    private VVoting feature;

    public VotePartyCommand(VVoting feature)
    {
        super(null, null, "Parent command for the VoteParty feature withing VVoting.", true, "voteparty", "vp");
        this.feature = feature;
        addChildren(new ClaimCommend(feature), new CountCommand(feature), new TimeCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}