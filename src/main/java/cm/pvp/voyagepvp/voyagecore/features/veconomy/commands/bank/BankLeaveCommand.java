package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.Collectors;

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

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyOwnerMessage;

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

        String[] split = arguments.get(0).split("/");
        String name;

        if (split.length == 1) {
            name = split[0];
        } else {
            name = split[1];
        }

        List<UUID> accounts = feature.get(p.getUniqueId()).getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equalsIgnoreCase(name)).collect(Collectors.toCollection(Lists::newArrayList));

        if (accounts.size() == 0) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        SharedAccount account;

        if (accounts.size() > 1) {
            if (split.length == 1) {
                sender.sendMessage(Format.colour(Format.format(specifyOwnerMessage, "{amount};" + String.valueOf(accounts.size()))));
                return;
            }

            Optional<PlayerProfile> target = feature.getInstance().getMojangLookup().lookup(split[0]);

            if (!target.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + split[0])));
                return;
            }

            account = accounts.stream().filter(id -> feature.getAccount(id).getOwner().equals(target.get().getId())).map(id -> feature.getAccount(id)).findFirst().get();
        } else  {
            account = feature.getAccount(accounts.get(0));
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
                feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), p.getUniqueId(), Action.LEAVE_BANK, new Date(), Maps.newHashMap()));
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
