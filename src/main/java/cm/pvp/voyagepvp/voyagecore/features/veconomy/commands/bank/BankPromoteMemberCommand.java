package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class BankPromoteMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String playerIsNotAMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerispoa")
    private String playerIsPOAMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.promotedplayer")
    private String promotedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
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
        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (account == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (!account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        if (account.getOwner().equals(target)) {
            sender.sendMessage(Format.colour(Format.format(playerIsOwnerMessage)));
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

        if (account.promoteMember(target).getResponse() == Response.SUCCESS) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("promotedMember", target);
            feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), p.getUniqueId(), Action.PROMOTE_MEMBER, new Date(), map));
            sender.sendMessage(Format.colour(Format.format(promotedPlayerMessage, "{target};" + arguments.get(0))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
