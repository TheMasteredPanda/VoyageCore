package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class AdminBankCommand extends VoyageCommand
{
    public AdminBankCommand(VEconomy feature)
    {
        super(null, null, "Parent command for admin bank commands", false, "bank", "vb");
        addChildren(new AddMemberCommand(feature), new BankBalanceClearCommand(feature),
                new DeleteBankCommand(feature), new DemoteMemberCommand(feature),
                new PromoteMemberCommand(feature), new RemoveMemberCommand(feature),
                new TransferOwnershipCommand(feature), new BankBalanceAddCommand(feature),
                new BankInfoCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
