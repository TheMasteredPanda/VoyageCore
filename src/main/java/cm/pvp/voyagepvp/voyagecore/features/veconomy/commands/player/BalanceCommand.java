package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

public class BalanceCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.balance")
    private String balanceMessage;

    public BalanceCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.balance", "Check your balance", true, "balance", "bal");
        this.feature = feature;
        feature.getInstance().getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        if (balanceMessage == null) {
            feature.getLogger().warning("The balance message is null.");
        }

        VEconomyPlayer player = feature.get(((Player) sender).getUniqueId());

        if (player == null) {
            feature.getLogger().warning("The VEconomyPlayer instance is null.");
        }

        sender.sendMessage(Format.colour(Format.format(balanceMessage, "{balance};" + feature.getVaultHook().format(feature.get(((Player) sender)).getAccount().getBalance()))));
    }
}
