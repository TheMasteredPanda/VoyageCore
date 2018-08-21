package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class VotePartyAdminCommand extends VoyageCommand
{
    public VotePartyAdminCommand(VVoting feature)
    {
        super(null, null, "Admin command for VoteParty", false, "votepartyadmin", "vpa");
        addChildren(new StartPartyCommand(feature), new StartPartyCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
