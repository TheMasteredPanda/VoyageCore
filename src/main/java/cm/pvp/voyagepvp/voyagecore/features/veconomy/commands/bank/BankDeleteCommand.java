package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class BankDeleteCommand extends VoyageCommand
{
    private VEconomy feature;

    private ArrayList<UUID> awaitingConfirmation = Lists.newArrayList();

    @ConfigPopulate("modules.veconomy.messages.bank.clarifyquestion")
    private String clarifyQuestionMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.bankdeleted")
    private String bankDeletedMessage;

    @ConfigPopulate("modules.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public BankDeleteCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.delete", "Delete a shared bank.", true, "delete");
        this.feature = feature;

        try {
            addArguments(new ArgumentField("name", true));
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;

        if (!awaitingConfirmation.contains(p.getUniqueId())) {
            awaitingConfirmation.add(p.getUniqueId());
            sender.sendMessage(Format.colour(Format.format(clarifyQuestionMessage, "{bank};" + arguments.get(0))));
            return;
        } else {
            awaitingConfirmation.remove(p.getUniqueId());
        }

        VEconomyPlayer player = feature.get(p.getUniqueId());

        UUID bank = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).findFirst().orElse(null);

        if (bank == null) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
        } else {
            double balance = feature.getAccount(bank).getBalance();
            feature.removeSharedAccount(bank);

            if (feature.get(p.getUniqueId()).getAccount().add(balance).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(bankDeletedMessage, "{amount};" + String.valueOf(balance))));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        }
    }
}
