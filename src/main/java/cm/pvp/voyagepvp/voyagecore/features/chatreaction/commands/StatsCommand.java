package cm.pvp.voyagepvp.voyagecore.features.chatreaction.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.math.TimeUtil;
import cm.pvp.voyagepvp.voyagecore.features.chatreaction.DataHandler;
import cm.pvp.voyagepvp.voyagecore.features.chatreaction.ReactionPlayer;
import com.google.common.base.Joiner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class StatsCommand extends VoyageCommand
{
    private DataHandler handler;

    private VoyageCore instance;

    @ConfigPopulate("features.chatreaction.messages.couldnotfinddata")
    private String couldNotFindData;

    @ConfigPopulate("features.chatreaction.messages.informationtemplate")
    private List<String> informationTemplate;

    @ConfigPopulate("features.chatreaction.messages.cannotfindplayerprofile")
    private String cannotFindPlayerProfile;

    public StatsCommand(VoyageCore instance, DataHandler handler)
    {
        super(null, "voyagecore.chatreaction.stats", "Check the statistics of yourself, or another player for CheatReaction.", false, "crstats");
        this.instance = instance;
        this.handler = handler;

        try {
            ArgumentField field = new ArgumentField("player name", true);
            field.setCheckFunction(new PlayerCheckFunction(instance.getMojangLookup()));
            addArguments(field);
            if (informationTemplate == null) {
                System.out.println("INFORMATION TEMPLATE IS NULL.");
            }
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Optional<ReactionPlayer> player;

        if (arguments.size() != 0 && sender.hasPermission("voyagecore.chatreaction.stats.others")) {
            Optional<PlayerProfile> profile = instance.getMojangLookup().lookup(arguments.get(0));

            if (!profile.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(cannotFindPlayerProfile, "{player};" + arguments.get(0))));
                return;
            } else {
                player = handler.get(profile.get().getId());
            }
        } else {
            player = handler.get(((Player) sender).getUniqueId());
        }

        if (!player.isPresent()) {
            sender.sendMessage(Format.colour(couldNotFindData));
            return;
        }

        ReactionPlayer reactionPlayer = player.get();
        informationTemplate.replaceAll(s -> s.replace("{wins}", String.valueOf(reactionPlayer.getWins())).replace("[fastesttime}", TimeUtil.millisecondsToTimeUnits(TimeUnit.MICROSECONDS, reactionPlayer.getFastest(), false)));
        sender.sendMessage(Joiner.on('\n').join(informationTemplate));
    }
}
