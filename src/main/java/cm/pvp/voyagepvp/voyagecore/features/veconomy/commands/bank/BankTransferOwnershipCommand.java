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

import static cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount.Type.MEMBER;

public class BankTransferOwnershipCommand extends VoyageCommand
{
    private VEconomy feature;

    private ArrayListMultimap<UUID, UUID> awaitingConfirmation = ArrayListMultimap.create();
    private ArrayList<CountdownTask> countdowns = Lists.newArrayList();

    @ConfigPopulate("modules.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.playerisowner")
    private String playerIsOwnerMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.nopermission")
    private String noPermissionMessage;

    @ConfigPopulate("movules.veconomy.messages.error")
    private String errorMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.transferredownership")
    private String tranferredBankMessage;

    @ConfigPopulate("modules.veconomy.messages.bank.transferownershipquestion")
    private String transferOwnershipQuestionMessage;

    public BankTransferOwnershipCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.transferownership", "Transfer the ownership of a bank to a member.", true, "transferownership");
        this.feature = feature;
        ArgumentField checkPlayer = new ArgumentField("player name", true);
        checkPlayer.setCheckFunction(new PlayerCheckFunction(feature.getInstance().getMojangLookup()));

        try {
            addArguments(new ArgumentField("bank name", true), checkPlayer);
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

        if (account.getMembers().get(p.getUniqueId()) == MEMBER) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        UUID target = feature.getInstance().getMojangLookup().lookup(arguments.get(1)).get().getId();

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

