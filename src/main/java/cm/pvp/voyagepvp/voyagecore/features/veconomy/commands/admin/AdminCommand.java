package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank.AdminBankCommand;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

public class AdminCommand extends VoyageCommand
{
    private VEconomy feature;

    public AdminCommand(VEconomy feature)
    {
        super(null, null, "Parent command for VEconomy admin commands.", true, "aveconomy", "aveco");
        this.feature = feature;
        addChildren(new BalanceCommand(feature), new AdminBankCommand(feature));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
    }
}
