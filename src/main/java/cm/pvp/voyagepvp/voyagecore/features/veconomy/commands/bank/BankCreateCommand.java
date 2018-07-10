package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
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

    @ConfigPopulate("features.veconomy.messages.bank.nameused")
    private String nameUsedMessage;

    @ConfigPopulate("features.veconomy.messages.bank.created")
    private String successfullyCreatedBank;

    @ConfigPopulate("features.veconomy.messages.bank.limitexceeded")
    private String bankLimitExceededMessage;

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
        int bankLimit = p.getEffectivePermissions().stream().filter(perm -> perm.getPermission().startsWith("voyagecore.veconomy.banks.")).map(perm -> NumberUtil.parse(perm.getPermission().split("\\.")[3], int.class)).findFirst().orElse(0);

        if (ownedBanks.size() == bankLimit) {
            sender.sendMessage(Format.colour(Format.format(bankLimitExceededMessage, "{limit};" + String.valueOf(bankLimit))));
            return;
        }

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
