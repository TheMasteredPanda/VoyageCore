package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount.Type.MEMBER;

public class BankTransferOwnershipCommand extends VoyageCommand
{
    private VEconomy feature;

    private HashMap<UUID, UUID> awaitingClarification = Maps.newHashMap();

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("movules.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.transferred")
    private String tranferredBankMessage;

    public BankTransferOwnershipCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.transferownership", "Transfer the ownership of a bank to a member.", true, "transferownership");
        this.feature = feature;
        ArgumentField checkPlayer = new ArgumentField("player name", true);
        checkPlayer.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(new ArgumentField("bank name", true), checkPlayer);
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer player = feature.get(p.getUniqueId());
        SharedAccount account = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);

        if (account == null) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
            return;
        }

        if (account.getMembers().get(p.getUniqueId()) == MEMBER) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (account.getOwner().equals(target)) {
            sender.sendMessage(Format.colour(playerIsOwnerMessage));
            return;
        }

        if (account.transferOwnership(target).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(tranferredBankMessage));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
