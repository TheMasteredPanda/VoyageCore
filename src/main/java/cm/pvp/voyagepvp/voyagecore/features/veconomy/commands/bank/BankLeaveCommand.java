package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class BankLeaveCommand extends VoyageCommand
{
    private VEconomy feature;

    private ArrayListMultimap<UUID, UUID> awaitingConfirmation = ArrayListMultimap.create();
    private ArrayList<CountdownTask> countdowns = Lists.newArrayList();

    @ConfigPopulate("features.veconomy.messages.bank.ownercannotleave")
    private String ownerCannotLeaveMessage;

    @ConfigPopulate("features.veconomy.messages.bank.left")
    private String leftBankMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisnotmember")
    private String playerIsNotMemberMessage;

    @ConfigPopulate("features.veconomy.messages.bank.leavequestion")
    private String leaveConfirmationQuestion;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    public BankLeaveCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.leave", "Leave a bank.", true, "leave");
        this.feature = feature;

        try {
            addArguments(new ArgumentField("bank name", true));
            feature.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        SharedAccount account = feature.get(p.getUniqueId()).getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equalsIgnoreCase(arguments.get(0))).map(id -> feature.getAccount(id)).findFirst().orElse(null);

        if (account == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (!account.isMember(p.getUniqueId())) {
            sender.sendMessage(Format.colour(playerIsNotMemberMessage));
            return;
        }

        if (account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(ownerCannotLeaveMessage));
            return;
        }

        if (!awaitingConfirmation.containsEntry(account.getId(), p.getUniqueId())) {
            awaitingConfirmation.put(account.getId(), p.getUniqueId());
            sender.sendMessage(Format.colour(Format.format(leaveConfirmationQuestion, "{bank};" + account.getName())));
            CountdownTask task = new CountdownTask(account.getOwner(), account.getId());
            countdowns.add(task);
        } else {
            awaitingConfirmation.remove(account.getId(), p.getUniqueId());
            CountdownTask countdownTask = countdowns.stream().filter(task -> task.getAccount().equals(account.getId()) && task.getOwner().equals(account.getOwner())).findFirst().get();
            countdownTask.cancel();
            countdowns.remove(countdownTask);

            if (account.removeMember(p.getUniqueId(), account.getMembers().get(p.getUniqueId())).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(leftBankMessage, "{bank};" + account.getName())));
                feature.get(p).getSharedAccounts().remove(account.getId());
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        }
    }

    public class CountdownTask extends BukkitRunnable
    {
        @Getter
        private UUID owner;

        @Getter
        private UUID account;

        private int countdown = 30;

        public CountdownTask(UUID owner, UUID account)
        {
            this.owner = owner;
            this.account = account;
            runTaskLaterAsynchronously(feature.getInstance(), 20L);
        }

        @Override
        public void run()
        {
            countdown--;

            if (countdown <= 0) {
                awaitingConfirmation.remove(owner, account);
                cancel();
                countdowns.remove(this);
            }
        }
    }
}
