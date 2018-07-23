package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.MembershipRequest;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Date;
import java.util.LinkedList;

public class BankInvitePlayerCommand extends VoyageCommand
{
    private VEconomy instance;

    @ConfigPopulate("features.veconomy.messages.bank.alreadyinvitedplayer")
    private String alreadyInvitedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.invitedplayer")
    private String invitedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.playerisalreadymember")
    private String playerIsAlreadyMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public BankInvitePlayerCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.player.bank.invite", "Invite a player to be a member of a bank.", true, "invite");
        this.instance = instance;

        ArgumentField playerArg = new ArgumentField("player name", true);
        playerArg.setCheckFunction(new PlayerCheckFunction(instance.getInstance().getMojangLookup()));

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

        SharedAccount bank = senderPlayer.getSharedAccounts().stream().filter(id -> instance.getAccount(id).getName().equalsIgnoreCase(arguments.get(0))).map(id -> instance.getAccount(id)).findFirst().orElse(null);

        if (bank == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (bank.getMembers().get(p.getUniqueId()) == SharedAccount.Type.MEMBER) {
            p.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        VEconomyPlayer target = instance.get(instance.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId());

        if (target.hasBeenInvitedTo(bank.getId())) {
            p.sendMessage(Format.colour(Format.format(alreadyInvitedPlayerMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (bank.isMember(target.getReference().get().getUniqueId())) {
            p.sendMessage(Format.colour(playerIsAlreadyMemberMessage));
            return;
        }

        target.addRequest(MembershipRequest.builder().accountId(bank.getId()).date(new Date()).requester(p.getUniqueId()).build());
        p.sendMessage(Format.colour(Format.format(invitedPlayerMessage, "{bank};" + arguments.get(0), "{target};" + arguments.get(1))));
    }
}
