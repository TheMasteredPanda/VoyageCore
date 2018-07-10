package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class BankCommand extends VoyageCommand
{
    public BankCommand(VEconomy feature)
    {
        super(null, null, "Parent command for the bank feature.", true, "bank", "vb");
        addChildren(new BankBalanceCommand(feature),
                new BankCreateCommand(feature),
                new BankDeleteCommand(feature),
                new BankListCommand(feature),
                new BankAddMemberCommand(feature),
                new BankDemoteMemberCommand(feature),
                new BankRemoveMemberCommand(feature),
                new BankPromoteMemberCommand(feature),
                new BankTransferOwnershipCommand(feature),
                new BankTransferCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
