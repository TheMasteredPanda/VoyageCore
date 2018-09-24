package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class StopPartyCommand extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("featurs.votting.voteparty.admin.partynotstarted")
    private String partyNotStarted;

    @ConfigPopulate("features.vvoting.voteparty.admin.partystopped")
    private String partyStoppedMessage;

    @ConfigPopulate("features.vvoting.votepary.admin.partystoppedannouncement")
    private String partyStoppedAccouncementMessage;

    public StopPartyCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteparty.admin.stopparty", "Stop a VoteParty.", true, "stop");
        this.feature = feature;
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (!feature.getSettingsObject().get("startedParty").getAsBoolean()) {
            sender.sendMessage(Format.colour(partyNotStarted));
            return;
        }

        feature.stopVotingParty();
        sender.sendMessage(Format.colour(partyStoppedMessage));
    }
}
