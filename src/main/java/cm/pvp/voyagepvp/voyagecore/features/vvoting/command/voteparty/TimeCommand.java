package cm.pvp.voyagepvp.voyagecore.features.vvoting.command.voteparty;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.TimeUtil;
import cm.pvp.voyagepvp.voyagecore.features.vvoting.VVoting;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class TimeCommand extends VoyageCommand
{
    private VVoting feature;

    @ConfigPopulate("features.vvoting.messages.voteparty.admin.partyalreadystarted")
    private String partyAlreadyStartedMessage;

    @ConfigPopulate("features.vvoting.messages.voteparty.timeleft")
    private String timeLeftMessage;

    @ConfigPopulate("features.vvoting.messages.voteparty.cooldownnotenabled")
    private String notEnabledMessage;

    public TimeCommand(VVoting feature)
    {
        super(null, "voyagecore.vvoting.voteparty.time", "Check when the next VotePary will start.", true, "time");
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
        if (feature.getSettingsObject().get("startedParty").getAsBoolean()) {
            sender.sendMessage(Format.colour(partyAlreadyStartedMessage));
            return;
        }

        sender.sendMessage(Format.colour(Format.format(timeLeftMessage, "{time};" + TimeUtil.millisecondsToTimeUnits(TimeUnit.SECONDS, feature.getCountdown().getInterval(), false))));
    }
}
