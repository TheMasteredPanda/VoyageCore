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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount.Type.MEMBER;

public class BankTransferOwnershipCommand extends VoyageCommand
{
    private VEconomy feature;

    private ArrayListMultimap<UUID, UUID> awaitingConfirmation = ArrayListMultimap.create();
    private ArrayList<CountdownTask> countdowns = Lists.newArrayList();

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.transferredownership")
    private String tranferredBankMessage;

    @ConfigPopulate("features.veconomy.messages.bank.transferownershipquestion")
    private String transferOwnershipQuestionMessage;

    public BankTransferOwnershipCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.transferownership", "Transfer the ownership of a bank to a member.", true, "transferownership");
        this.feature = feature;

        addArguments(new ArgumentField("bank name", true), new ArgumentField("player name", true).check(new PlayerCheckFunction(feature.getInstance().getBackupLookup())));
        feature.getInstance().getMainConfig().populate(this);
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

        if (account.getMembers().get(p.getUniqueId()) == MEMBER) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        UUID target = feature.getInstance().getBackupLookup().lookup(arguments.get(1)).get().getId();

        if (account.getOwner().equals(target)) {
            sender.sendMessage(Format.colour(playerIsOwnerMessage));
            return;
        }

        if (!awaitingConfirmation.containsEntry(p.getUniqueId(), account.getId())) {
            awaitingConfirmation.put(p.getUniqueId(), account.getId());
            countdowns.add(new CountdownTask(p.getUniqueId(), account.getId()));
            sender.sendMessage(Format.colour(Format.format(transferOwnershipQuestionMessage)));
        } else {
            awaitingConfirmation.remove(p.getUniqueId(), account.getId());
            CountdownTask task = countdowns.stream().filter(t -> t.getAccount().equals(account.getId()) && t.getOwner().equals(p.getUniqueId())).findFirst().orElse(null);

            if (task != null) {
                if (Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())) {
                    task.cancel();
                }

                countdowns.remove(task);
            }

            if (account.transferOwnership(target).getResponse() == Response.SUCCESS) {
                feature.getHandler().addUserHistoryEntry(new HistoryEntry(account.getId(), p.getUniqueId(), Action.TRANSFER_OWNERSHIP, new Date(), ImmutableMap.<String, Object>builder().put("transferredOwnershipTo", target.toString()).build()));
                sender.sendMessage(Format.colour(tranferredBankMessage));
            } else {
                sender.sendMessage(Format.colour(errorMessage));
            }
        }

    }

    class CountdownTask extends BukkitRunnable
    {
        @Getter
        private UUID owner;

        @Getter
        private UUID account;


        private int countdown = 30;

        CountdownTask(UUID owner, UUID account)
        {
            this.owner = owner;
            this.account = account;
            runTaskTimerAsynchronously(feature.getInstance(), 0L, 20L);
        }

        @Override
        public void run()
        {
            countdown--;

            if (countdown <= 0) {
                awaitingConfirmation.get(owner).remove(account);
                cancel();
                countdowns.remove(this);
            }
        }
    }
}

