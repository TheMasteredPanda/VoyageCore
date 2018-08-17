package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.DataHandler;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class BankLedgerCommand extends VoyageCommand
{
    private DataHandler handler;
    private VEconomy instance;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeForat = new SimpleDateFormat("HH:mm:ss");

    @ConfigPopulate("features.veconomy.messages.ledger.entry")
    private String ledgerEntry;

    @ConfigPopulate("features.veconomy.messages.noentries")
    private String noEntriesMessage;

    @ConfigPopulate("features.veconomy.messages.ledger.header")
    private String ledgerHeader;

    @ConfigPopulate("features.veconomy.messages.ledger.footer")
    private String ledgerFooter;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.incorrectdateformat")
    private String incorrectDateFormatMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    public BankLedgerCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.player.bank.ledger", "Command to view a banks ledger", true, "ledger");
        this.instance = instance;
        this.handler = instance.getHandler();

        try {
            instance.getInstance().getMainConfig().populate(this);
            addArguments(new ArgumentField("bank name", true), new ArgumentField("date (yyyy/mm/dd)", false));
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        VEconomyPlayer player = instance.get(p.getUniqueId());
        UUID id = player.getSharedAccounts().stream().filter(id1 -> instance.getAccount(id1).getName().equalsIgnoreCase(arguments.get(0))).findFirst().orElse(null);

        if (id == null) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
            return;
        }

        SharedAccount account = instance.getAccount(id);

        if (account.isMember(p.getUniqueId()) || !account.getMembers().get(p.getUniqueId()).equals(SharedAccount.Type.POA) || !account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        if (arguments.size() == 1) {
            handler.getEntireLedger(id).whenCompleteAsync((entries, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(Format.format(noEntriesMessage, "{bank};" + arguments.get(0))));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();
                message.add(Format.colour(ledgerHeader));

                for (SharedLedgerEntry entry : entries) {
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getPlayer()).orElse(null);

                    if (targetProfile == null) {
                        throw new MojangException("Couldn't get the username of " + entry.getPlayer().toString());
                    }

                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + instance.getFancyActionName(entry.getAction()), "{player};" + targetProfile.getName(),
                            "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()),
                            "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeForat.format(entry.getDate()),
                            "{addedorremoved};" + (entry.getAction() != Action.DEPOSIT_MONEY ? "Removed" : "Added"))));
                }

                message.add(Format.colour(ledgerFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        } else {
            Date date = null;

            try {
                date = dateFormat.parse(arguments.get(1));
            } catch (ParseException e) {
                sender.sendMessage(Format.colour(Format.format(incorrectDateFormatMessage, "{argument};" + arguments.get(1))));
                return;
            }

            handler.getLedgersFrom(id, date).whenCompleteAsync((entries, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(Format.format(noEntriesMessage, "{bank};" + arguments.get(0))));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();
                message.add(Format.colour(ledgerHeader));

                for (SharedLedgerEntry entry : entries) {
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getPlayer()).orElse(null);

                    if (targetProfile == null) {
                        throw new MojangException("Couldn't get the username of " + entry.getPlayer().toString() + ".");
                    }

                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + instance.getFancyActionName(entry.getAction()), "{player};" + targetProfile.getName(), "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()), "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeForat.format(entry.getDate()))));
                }

                message.add(Format.colour(ledgerFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        }
    }
}
