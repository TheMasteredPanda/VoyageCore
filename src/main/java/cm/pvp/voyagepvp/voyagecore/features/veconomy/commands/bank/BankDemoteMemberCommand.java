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

public class BankDemoteMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.demotedplayer")
    private String demotedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerismember")
    private String playerIsMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String playerIsNotAMemberMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermission;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public BankDemoteMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.demotemember", "Demote a member from a POA to a Member.", true, "demote");
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
        SharedAccount account = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);
        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (account == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (account.getOwner().equals(target)) {
            sender.sendMessage(Format.colour(playerIsOwnerMessage));
            return;
        }

        if (!account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(noPermission));
            return;
        }

        if (!account.isMember(target)) {
            sender.sendMessage(Format.colour(Format.format(playerIsNotAMemberMessage, "{target};" + arguments.get(1))));
            return;
        }

        if (account.getMembers().get(target) == SharedAccount.Type.MEMBER) {
            sender.sendMessage(Format.colour(playerIsMemberMessage));
            return;
        }

        if (account.demoteMember(target).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(demotedPlayerMessage, "{target};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
