package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.Collectors;

import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount.Type.MEMBER;
import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount.Type.POA;

public class BankRemoveMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String playerIsNotAMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.removedmember")
    private String removedMemberMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.specifyaccountowner")
    private String specifyBankOwnerMessage;

    public BankRemoveMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.removemember", "Remove a member from the bank.", true, "removemember");
        this.feature = feature;
        ArgumentField bankCheck = new ArgumentField("bank name", true);
        ArgumentField playerCheck = new ArgumentField("player name", true);
        playerCheck.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(bankCheck, playerCheck);
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

        String[] split = arguments.get(0).split("/");
        String bankName;

        if (split.length == 1) {
            bankName = split[0];
        } else {
            bankName = split[1];
        }

        List<UUID> accounts = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equalsIgnoreCase(bankName)).collect(Collectors.toCollection(Lists::newArrayList));

        if (accounts.size() == 0) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
            return;
        }

        SharedAccount account;

        if (accounts.size() > 1) {
            if (split.length == 1) {
                sender.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount};" + String.valueOf(accounts.size()))));
                return;
            }


            Optional<PlayerProfile> target = feature.getInstance().getMojangLookup().lookup(split[0]);

            if (!target.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + split[0])));
                return;
            }

            account = accounts.stream().filter(id -> feature.getAccount(id).getOwner().equals(target.get().getId())).map(id -> feature.getAccount(id)).findFirst().get();
        } else {
            account = feature.getAccount(accounts.get(0));
        }


        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (account.getMembers().get(p.getUniqueId()) == MEMBER) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }


        if ((account.getOwner().equals(target) || account.getMembers().get(target) == MEMBER) && account.getMembers().get(p.getUniqueId()) == POA) {
            sender.sendMessage(playerIsNotAMemberMessage);
            return;
        }

        if (account.removeMember(target, account.getMembers().get(target)).getResponse() == Response.SUCCESS) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("removedMember", target.toString());
            feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), p.getUniqueId(), Action.REMOVE_MEMBER, new Date(), map));
            sender.sendMessage(Format.colour(Format.format(removedMemberMessage, "{target};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
