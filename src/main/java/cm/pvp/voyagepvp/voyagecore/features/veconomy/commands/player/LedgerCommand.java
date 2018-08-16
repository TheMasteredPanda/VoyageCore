package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.player;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.PlayerLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class LedgerCommand extends VoyageCommand
{
    private VEconomy instance;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @ConfigPopulate("features.veconomy.messages.noentries")
    private String noEntriesMessage;

    @ConfigPopulate("features.veconomy.messages.ledger.header")
    private String ledgerHeader;

    @ConfigPopulate("features.veconomy.messages.ledger.footer")
    private String ledgerFooter;

    @ConfigPopulate("features.veconomy.messages.ledger.entry")
    private String ledgerEntry;

    @ConfigPopulate("features.veconomy.messages.incorrectdateformat")
    private String incorrectDateFormatMessage;

    private LedgerCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.player.ledger", "View the ledger for your own personal account.", true, "ledger");
        this.instance = instance;

        try {
            addArguments(new ArgumentField("date (yyyy/mm/dd", false));
            instance.getInstance().getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;

        if (arguments.size() == 0) {
            instance.getHandler().getEntirePersonalLedger(p.getUniqueId()).whenCompleteAsync((entries, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(Format.format(noEntriesMessage, "{bank};" + arguments.get(0))));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();
                message.add(Format.colour(ledgerHeader));

                for (PlayerLedgerEntry entry : entries) {
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getPlayer()).orElse(null);

                    if (targetProfile == null) {
                        throw new MojangException("Couldn't get the username of " + entry.getPlayer().toString());
                    }

                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + instance.getFancyActionName(entry.getAction()), "{player};" + targetProfile.getName(),
                            "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()),
                            "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()),
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

            instance.getHandler().getPersonalLedgersFrom(p.getUniqueId(), date).whenCompleteAsync((entries, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(Format.format(noEntriesMessage, "{bank};" + arguments.get(0))));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();
                message.add(Format.colour(ledgerHeader));

                for (PlayerLedgerEntry entry : entries) {
                    PlayerProfile targetProfile = instance.getInstance().getMojangLookup().lookup(entry.getPlayer()).orElse(null);
                    message.add(Format.colour(Format.format(ledgerEntry, "{action};" + instance.getFancyActionName(entry.getAction()), "{player};" + (targetProfile == null ? "Console" : targetProfile.getName()), "{balance};" + String.valueOf(entry.getBalance()), "{amount};" + String.valueOf(entry.getAmount()), "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()))));
                }

                message.add(Format.colour(ledgerFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        }
    }
}