package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank.BankCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player.BalanceCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player.TransferCommand;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class VEconomyCommand extends VoyageCommand
{
    public VEconomyCommand(VEconomy feature)
    {
        super(null, null, "Parent command for VEconomy", true, "veconomy", "veco", "eco");
        addChildren(new BalanceCommand(feature), new TransferCommand(feature), new BankCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
