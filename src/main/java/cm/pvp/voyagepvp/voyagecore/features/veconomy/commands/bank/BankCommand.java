package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class BankCommand extends VoyageCommand
{
    public BankCommand(VEconomy feature)
    {
        super(null, null, "Parent command for the bank feature.", true, "vbank", "bank", "vb");
        addChildren(new BankBalanceCommand(feature), new BankCreateCommand(feature), new BankDeleteCommand(feature), new BankDeleteCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
