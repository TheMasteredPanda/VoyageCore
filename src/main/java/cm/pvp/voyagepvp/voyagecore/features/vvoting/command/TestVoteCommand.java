package cm.pvp.voyagepvp.voyagecore.features.vvoting.command;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.LinkedList;

public class TestVoteCommand extends VoyageCommand
{
    public TestVoteCommand()
    {
        super(null, "voyagecore.vvoting.testvote", "Casts a test vote", false, "testvote");

        addArguments(new ArgumentField("name", true));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Date date = new Date();
        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(new Vote("Test Service", arguments.get(0), "127.0.0.1", date.toString())));
        sender.sendMessage("Sent a test vote.");
    }
}
