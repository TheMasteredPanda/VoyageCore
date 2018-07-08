package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BankCreateCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("modules.veconomy.messages.bank.nameused")
    private String nameUsedMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.created")
    private String successfullyCreatedBank;

    public BankCreateCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.create", "Create a new shared bank.", true, "create");
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
        Player p = (Player)sender;
        VEconomyPlayer player = feature.get(p.getUniqueId());
        List<UUID> ownedBanks = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getOwner().equals(p.getUniqueId())).collect(Collectors.toCollection(Lists::newArrayList));

        for (UUID ownedBank : ownedBanks) {
            SharedAccount account = feature.getAccount(ownedBank);

            if (!account.getName().equals(arguments.get(0))) {
                continue;
            }

            sender.sendMessage(Format.colour(Format.format(nameUsedMessage, "{name};" + arguments.get(0))));
            return;
        }

        feature.createSharedAccount(p.getUniqueId(), arguments.get(0));
        sender.sendMessage(Format.colour(Format.format(successfullyCreatedBank, "{name};" + arguments.get(0))));
    }
}
