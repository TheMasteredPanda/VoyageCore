package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BalanceCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("modules.veconomy.messages.balance")
    private String balanceMessage;

    public BalanceCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.balance", "Check your balance", true, "balance", "bal");
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
        sender.sendMessage(Format.colour(Format.format(balanceMessage, "{balance};" + String.valueOf(feature.get(((Player) sender)).getAccount().getBalance()))));
    }
}
