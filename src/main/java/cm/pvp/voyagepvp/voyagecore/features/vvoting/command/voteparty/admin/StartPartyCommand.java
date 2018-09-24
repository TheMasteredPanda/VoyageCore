package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class StartPartyCommand extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.messages.voteparty.admin.partyalreadystarted")
    private String partyAlreadyStartedMessage;

    @ConfigPopulate("features.vvoting.messages.voteparty.admin.partystarted")
    private String partyStartedMessage;

    public StartPartyCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteparty.admin.startparty", "Start a vote party.", false, "start");
        this.feature = feature;
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (feature.getSettingsObject().get("startedParty").getAsBoolean()) {
            sender.sendMessage(Format.colour(partyAlreadyStartedMessage));
            return;
        }

        feature.startVotingParty();
        sender.sendMessage(Format.colour(partyStartedMessage));
    }
}
