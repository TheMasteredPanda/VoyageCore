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
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class BankAddMemberCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.playerisalreadymember")
    private String playerIsAlreadyAMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.addedplayer")
    private String addedPlayerMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    public BankAddMemberCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.addmember", "Add a member to a bank.", true, "addmember", "addm");
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
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        UUID id = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

        if (account.isMember(id)) {
            sender.sendMessage(Format.colour(Format.format(playerIsAlreadyAMemberMessage, "{target};" + arguments.get(1))));
        } else {
            account.addMember(id, SharedAccount.Type.MEMBER);
            VEconomyPlayer target = feature.get(id);
            target.getSharedAccounts().add(account.getId());
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("memberAdded", target.getReference().get().getUniqueId());
            feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), id, Action.ADD_MEMBER, new Date(), map));
            sender.sendMessage(Format.colour(Format.format(addedPlayerMessage, "{target};" + arguments.get(1))));
        }
    }
}
