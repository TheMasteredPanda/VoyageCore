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

    @ConfigPopulate("features.veconomy.messages.ledger.entry")
    private String ledgerEntry;

    @ConfigPopulate("features.veconomy.messages.ledger.noentries")
    private String noEntriesMessage;

    @ConfigPopulate("features.veconomy.messages.ledger.header")
    private String ledgerHeader;

    @ConfigPopulate("features.veconomy.messages.ledger.footer")
    private String ledgerFooter;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.ledger.incorrectdateformat")
    private String incorrectDateFormatMessage;

    public BankLedgerCommand(VEconomy instance)
    {
        super(null, "voyagecore.player.bank.ledger", "Command to view a banks ledger", true, "ledger");
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
        VEconomyPlayer player = instance.get(((Player) sender).getUniqueId());
        UUID id = player.getSharedAccounts().stream().filter(id1 -> instance.getAccount(id1).getName().equalsIgnoreCase(arguments.get(0))).findFirst().orElse(null);

        if (id == null) {
            sender.sendMessage(Format.colour(bankNotFoundMessage));
            return;
        }

        if (arguments.size() == 1) {
            handler.getEntireLedger(id).whenComplete((entries, throwable) -> {
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
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getMember()).orElse(null);

                    if (targetProfile == null) {
                        throw new MojangException("Couldn't get the username of " + entry.getMember().toString());
                    }

                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + entry.getAction().name(), "{member};" + targetProfile.getName(), "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()))));
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

            handler.getLedgersFrom(id, date).whenComplete((entries, throwable) -> {
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
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getMember()).orElse(null);

                    if (targetProfile == null) {
                        throw new MojangException("Couldn't get the username of " + entry.getMember().toString() + ".");
                    }

                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + entry.getAction().name(), "{member};" + targetProfile.getName(), "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()))));
                }

                message.add(Format.colour(ledgerFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        }
    }
}
