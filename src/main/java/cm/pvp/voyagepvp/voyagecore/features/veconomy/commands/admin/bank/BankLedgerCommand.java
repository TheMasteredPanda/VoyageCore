package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.admin.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.exception.MojangException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.DataHandler;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.PlayerLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.ledger.entry.SharedLedgerEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankLedgerCommand extends VoyageCommand
{
    private DataHandler handler;
    private VEconomy instance;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @ConfigPopulate("features.veconomy.messages.bank.ledger.entry.withdrew")
    private String withdrewEntry;

    @ConfigPopulate("features.veconomy.messages.bank.ledger.entry.deposited")
    private String depositedEntry;

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

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyBankMessage;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    public BankLedgerCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.admin.bank.ledger", "Command to view a banks ledger", true, "ledger");
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
        UUID id = null;
        String[] split = arguments.get(0).split("/");
        List<UUID> bankIds;


        if (split.length == 2) {
            bankIds = instance.getHandler().getSharedAccountsNamed(split[1]);
        } else {
            bankIds = instance.getHandler().getSharedAccountsNamed(split[0]);
        }

        if (bankIds.size() == 0) {
            sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
            return;
        }

        if (split.length == 1 && bankIds.size() > 1) {
            sender.sendMessage(Format.colour(Format.format(specifyBankMessage, "{amount};" + String.valueOf(bankIds.size()))));
            return;
        }

        if (split.length == 2) {
            Optional<PlayerProfile> targetProfile = instance.getInstance().getBackupLookup().lookup(split[0]);

            if (!targetProfile.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + split[0])));
                return;
            }

            VEconomyPlayer targetPlayer = instance.get(targetProfile.get().getId());
            UUID account = targetPlayer.getSharedAccounts().stream().filter(id1 -> instance.getAccount(id1).getName().equalsIgnoreCase(split[1])).findFirst().orElse(null);

            if (account == null) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
                return;
            }

            id = account;
        } else if (split.length == 1) {
            id = bankIds.get(0);
        }

        if (arguments.size() == 1) {
            instance.getHandler().getEntireLedger(id).whenCompleteAsync((entries, throwable) -> {
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
                    String messageEntry = null;

                    if (entry.getAction().equals(Action.WITHDRAW_MONEY)) {
                        String destination;

                        if ((boolean) entry.getData().get("destinationIsBank")) {
                            destination = "Bank " + entry.getData().get("destination");
                        } else {
                            Optional<PlayerProfile> player1 = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("destination")));

                            if (!player1.isPresent()) {
                                throw new MojangException("Couldn't get " + entry.getData().get("destination") + " from Mojang DB.");
                            }

                            destination = player1.get().getName();
                        }

                        messageEntry = Format.format(withdrewEntry, "{amount};" + String.valueOf(entry.getAmount()), "{balance};" + String.valueOf(entry.getBalance()),
                                "{action};" + instance.getFancyActionName(entry.getAction()), "{date};" + dateFormat.format(entry.getDate()),
                                "{time};" + timeFormat.format(entry.getDate()), "{destination};" + destination);
                    }

                    if (entry.getAction().equals(Action.DEPOSIT_MONEY)) {
                        String origin;

                        if ((boolean) entry.getData().get("originIsBank")) {
                            origin = "Bank " + entry.getData().get("origin");
                        } else {
                            Optional<PlayerProfile> playerOrigin = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("origin")));

                            if (!playerOrigin.isPresent()) {
                                throw new MojangException("Couldn't find " + entry.getData().get("origin") + " in Mojang DB.");
                            }

                            origin = playerOrigin.get().getName();
                        }

                        messageEntry = Format.format(depositedEntry, "{amount};" + String.valueOf(entry.getAmount()), "{balance};" + String.valueOf(entry.getBalance()),
                                "{action};" + instance.getFancyActionName(entry.getAction()), "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{origin};" + origin);

                    }

                    if (messageEntry == null) {
                        continue;
                    }

                    message.add(messageEntry);
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

            instance.getHandler().getPersonalLedgersFrom(id, date).whenCompleteAsync((entries, throwable) -> {
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
                    String messageEntry = null;

                    if (entry.getAction().equals(Action.WITHDRAW_MONEY)) {
                        String destination;

                        if ((boolean) entry.getData().get("destinationIsBank")) {
                            destination = "Bank " + entry.getData().get("destination");
                        } else {
                            Optional<PlayerProfile> player1 = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("destination")));

                            if (!player1.isPresent()) {
                                throw new MojangException("Couldn't get " + entry.getData().get("destination") + " from Mojang DB.");
                            }

                            destination = player1.get().getName();
                        }

                        messageEntry = Format.format(withdrewEntry, "{amount};" + String.valueOf(entry.getAmount()), "{balance};" + String.valueOf(entry.getBalance()),
                                "{action};" + instance.getFancyActionName(entry.getAction()), "{date};" + dateFormat.format(entry.getDate()),
                                "{time};" + timeFormat.format(entry.getDate()), "{destination};" + destination);
                    }

                    if (entry.getAction().equals(Action.DEPOSIT_MONEY)) {
                        String origin;

                        if ((boolean) entry.getData().get("originIsBank")) {
                            origin = "Bank " + entry.getData().get("origin");
                        } else {
                            Optional<PlayerProfile> playerOrigin = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("origin")));

                            if (!playerOrigin.isPresent()) {
                                throw new MojangException("Couldn't find " + entry.getData().get("origin") + " in Mojang DB.");
                            }

                            origin = playerOrigin.get().getName();
                        }

                        messageEntry = Format.format(depositedEntry, "{amount};" + String.valueOf(entry.getAmount()), "{balance};" + String.valueOf(entry.getBalance()),
                                "{action};" + instance.getFancyActionName(entry.getAction()), "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{origin};" + origin);

                    }

                    if (messageEntry == null) {
                        continue;
                    }

                    message.add(messageEntry);
                }

                message.add(Format.colour(ledgerFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        }
    }
}
