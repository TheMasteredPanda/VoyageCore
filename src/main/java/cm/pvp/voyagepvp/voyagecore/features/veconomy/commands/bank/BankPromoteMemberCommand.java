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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.UUID;

public class BankPromoteMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("modules.veconomy.messages.bank.playerisnotmember")
    private String playerIsNotAMemberMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.playerispoa")
    private String playerIsPOAMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("modules.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.promotedplayer")
    private String promotedPlayerMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public BankPromoteMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.promotemember", "Promote a member to POA", true, "promote");
        this.feature = feature;
        ArgumentField checkBank = new ArgumentField("bank name", true);
        ArgumentField playerCheck = new ArgumentField("player name", true);
        playerCheck.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(checkBank, playerCheck);
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
        UUID target = feature.getInstance().getMojangLookup().lookup(p.getUniqueId()).get().getId();

        if (account == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (!account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        if (account.getOwner().equals(target)) {
            sender.sendMessage(Format.format(playerIsOwnerMessage));
            return;
        }

        if (!account.isMember(target)) {
            sender.sendMessage(Format.colour(Format.format(playerIsNotAMemberMessage, "{target};" + arguments.get(0))));
            return;
        }

        if (account.getMembers().get(target) == SharedAccount.Type.POA) {
            sender.sendMessage(Format.colour(Format.format(playerIsPOAMessage, "{target};" + arguments.get(0))));
            return;
        }

        if (account.demoteMember(target).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(promotedPlayerMessage, "{target};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
