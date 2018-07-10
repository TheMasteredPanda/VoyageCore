package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class BalanceCommand extends VoyageCommand
{
    public BalanceCommand(VEconomy feature)
    {
        super(null, null, "Parent command for admin player balance commands", false, "balance", "bal");
        addChildren(new BalanceAddCommand(feature), new BalanceClearCommand(feature), new BalanceRemoveCommand(feature), new BalanceResetCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
