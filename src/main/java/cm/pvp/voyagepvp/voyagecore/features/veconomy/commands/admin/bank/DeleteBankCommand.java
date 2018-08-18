package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeleteBankCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.admin.bank.deleted")
    private String bankDeletedMessage;


    public DeleteBankCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.admin.bank.delete", "Delete a players bank.", true, "delete");
        this.feature = feature;

        ArgumentField playerArg = new ArgumentField("owners name (player name)", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getBackupLookup()));

        try {
            addArguments(playerArg, new ArgumentField("bank name ", true));
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        VEconomyPlayer player = feature.get(feature.getInstance().getBackupLookup().lookup(arguments.get(0)).get().getId());
        List<UUID> ownedBanks = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(player.getReference().get().getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        if (ownedBanks.size() == 0 || ownedBanks.stream().noneMatch(id -> feature.getAccount(id).getName().equals(arguments.get(1)))) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(1), "{target};" + arguments.get(0))));
            return;
        }

        UUID id = ownedBanks.stream().filter(id1 -> feature.getAccount(id1).getName().equals(arguments.get(1))).findFirst().orElse(null);
        feature.removeSharedAccount(id);
        sender.sendMessage(Format.colour(Format.format(bankDeletedMessage, "{bank};" + arguments.get(1), "{target}:" + arguments.get(0))));
    }
}
