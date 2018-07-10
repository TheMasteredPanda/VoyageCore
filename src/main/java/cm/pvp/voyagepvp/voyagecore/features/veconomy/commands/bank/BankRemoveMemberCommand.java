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

import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount.Type.MEMBER;
import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount.Type.POA;

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

    public BankRemoveMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.removemember", "Remove a member from the bank.", true, "remove");
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

        if (account == null) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
            return;
        }

        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (!account.getOwner().equals(p.getUniqueId()) || !account.getMembers().get(p.getUniqueId()).equals(POA)) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        if ((account.getOwner().equals(target) || account.getMembers().get(target) == MEMBER) && account.getMembers().get(p.getUniqueId()) == POA) {
            sender.sendMessage(playerIsNotAMemberMessage);
            return;
        }

        if (account.removeMember(target, account.getMembers().get(target)).getResponse() == Response.SUCCESS) {
            sender.sendMessage(Format.colour(Format.format(removedMemberMessage, "{target};" + arguments.get(1))));
        } else {
            sender.sendMessage(Format.colour(errorMessage));
        }
    }
}
