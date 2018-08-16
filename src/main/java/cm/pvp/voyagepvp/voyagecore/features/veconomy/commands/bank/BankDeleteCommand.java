package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Response;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

public class BankDeleteCommand extends VoyageCommand
{
    private VEconomy feature;

    private ArrayListMultimap<UUID, UUID> awaitingConfirmation = ArrayListMultimap.create();
    private ArrayList<CountdownTask> countdowns = Lists.newArrayList();

    @ConfigPopulate("features.veconomy.messages.bank.deletebankquestion")
    private String questionMessage;

    @ConfigPopulate("features.veconomy.messages.bank.bankdeleted")
    private String bankDeletedMessage;

    @ConfigPopulate("features.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    public BankDeleteCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.delete", "Delete a shared bank.", true, "delete");
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
        VEconomyPlayer player = feature.get(p.getUniqueId());
        UUID bank = player.getSharedAccounts().stream().filter(id -> feature.getAccount(id).getName().equals(arguments.get(0))).findFirst().orElse(null);
        if (bank == null) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
        } else {
            if (!feature.getAccount(bank).getOwner().equals(p.getUniqueId())) {
                sender.sendMessage(Format.colour(noPermissionMessage));
                return;
            }

            if (!awaitingConfirmation.containsEntry(p.getUniqueId(), bank)) {
                awaitingConfirmation.put(p.getUniqueId(), bank);
                countdowns.add(new CountdownTask(p.getUniqueId(), bank));
                sender.sendMessage(Format.colour(Format.format(questionMessage, "{bank};" + arguments.get(0))));
                return;
            } else {
                awaitingConfirmation.remove(p.getUniqueId(), bank);
                CountdownTask task = countdowns.stream().filter(t -> t.getAccount().equals(bank) && t.getOwner().equals(p.getUniqueId())).findFirst().orElse(null);

                if (task != null) {
                    if (!Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())) {
                        task.cancel();
                    }

                    countdowns.remove(task);
                }
            }

            double balance = feature.getAccount(bank).getBalance();
            feature.removeSharedAccount(bank);

            if (feature.get(p.getUniqueId()).getAccount().add(balance, null).getResponse() == Response.SUCCESS) {
                sender.sendMessage(Format.colour(Format.format(bankDeletedMessage, "{bank};" + arguments.get(0),"{amount};" + feature.getVaultHook().format(balance))));
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
