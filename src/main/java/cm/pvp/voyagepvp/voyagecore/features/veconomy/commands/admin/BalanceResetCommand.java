package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;

public class BalanceResetCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.admin.resetbalance")
    private String resetBalanceMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.startamount")
    private double startAmount;

    public BalanceResetCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.balance.reset", "Reset a players balance to the starting amount", true, "reset");

        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));


        try {
            addArguments(playerArg);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getMojangLookup().lookup(arguments.get(0)).get().getId());

        double subtractionAmount = player.getAccount().getBalance() - startAmount;

        if (player.getAccount().subtract(subtractionAmount, ((Player) sender).getUniqueId()).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(resetBalanceMessage, "{target};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
