package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.Collectors;

public class BankInvitePlayerCommand extends VoyageCommand
{
    private VEconomy instance;

    @ConfigPopulate("features.veconomy.messages.bank.alreadyinvitedplayer")
    private String alreadyInvitedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.invitedplayer")
    private String invitedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisalreadymember")
    private String playerIsAlreadyMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyBankOwnerMessage;

    public BankInvitePlayerCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.player.bank.invite", "Invite a player to be a member of a bank.", true, "invite");
        this.instance = instance;

        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(instance.getInstance().getBackupLookup()));

        try {
            addArguments(new ArgumentField("bank name", true), playerArg);
            instance.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer senderPlayer = instance.get(p.getUniqueId());
        String[] split = arguments.get(0).split("/");
        String name;

        if (split.length == 1) {
            name = split[0];
        } else {
            name = split[1];
        }

        List<UUID> bankIds = senderPlayer.getSharedAccounts().stream().filter(id -> instance.getAccount(id).getName().equalsIgnoreCase(name)).collect(Collectors.toCollection(Lists::newArrayList));
        SharedAccount bank = null;

        if (bankIds.size() == 0) {

            return;
        }

        if (bankIds.size() > 1) {
            if (split.length == 1) {
                p.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount}", String.valueOf(bankIds.size()))));
                return;
            }

            Optional<PlayerProfile> owner = instance.getInstance().getBackupLookup().lookup(split[0]);

            if (!owner.isPresent()) {
                p.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + split[0])));
                return;
            }

            bank = bankIds.stream().filter(id -> instance.getAccount(id).getOwner().equals(owner.get().getId())).map(id -> instance.getAccount(id)).findFirst().orElse(null);
        } else {
            bank = instance.getAccount(bankIds.get(0));
        }


        if (bank == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (bank.getMembers().get(p.getUniqueId()) == SharedAccount.Type.MEMBER) {
            p.sendMessage(Format.colour(noPermissionMessage));
            return;
        }


        Player player = Players.get(arguments.get(1));

        if (player == null) {
            p.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + arguments.get(1))));
            return;
        }

        VEconomyPlayer target = instance.get(player.getUniqueId());

        if (target.hasBeenInvitedTo(bank.getId())) {
            p.sendMessage(Format.colour(Format.format(alreadyInvitedPlayerMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (bank.isMember(target.getReference().get().getUniqueId())) {
            p.sendMessage(Format.colour(playerIsAlreadyMemberMessage));
            return;
        }

        if (target.addRequest(MembershipRequest.builder().accountId(bank.getId()).date(new Date()).requester(p.getUniqueId()).build()).getResponse() == Response.SUCCESS) {
            instance.getHandler().addUserHistoryEntry(new HistoryEntry(bank.getId(), p.getUniqueId(), Action.INVITED_MEMBER, new Date(), ImmutableMap.<String, Object>builder().put("invitedMember", target.getReference().get().getUniqueId().toString()).build()));
            p.sendMessage(Format.colour(Format.format(invitedPlayerMessage, "{bank};" + arguments.get(0), "{target};" + arguments.get(1))));
        }

    }
}