package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.HistoryEntry;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.response.Action;
import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankUserHistoryCommand extends VoyageCommand
{
    private VEconomy instance;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @ConfigPopulate("features.veconomy.messages.bank.history.header")
    private String histroyHeader;

    @ConfigPopulate("features.veconomy.messages.bank.history.footer")
    private String historyFooter;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.promoted")
    private String promotedEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.demoted")
    private String demotedEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.invite-rejected")
    private String inviteRejectedEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.invite-accepted")
    private String inviteAcceptedEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.ownership-transfer")
    private String ownershipTransferredEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.invite")
    private String inviteEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.removed")
    private String removedEntry;

    @ConfigPopulate("features.veconomy.messages.bank.history.entry.left")
    private String leftEntry;

    @ConfigPopulate("features.veconomy.messages.bank.notfound")
    private String bankNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.noentries")
    private String noEntriesMessage;

    @ConfigPopulate("features.veconomy.messages.playernotfound")
    private String playerNotFoundMessage;

    @ConfigPopulate("features.veconomy.messages.bank.specifyaccountowner")
    private String specifyBankOwnerMessage;

    @ConfigPopulate("features.veconomy.messages.incorrectdateformat")
    private String incorrectDateFormatMessage;

    @ConfigPopulate("features.veconomy.messages.nopermission")
    private String noPermissionMessage;

    public BankUserHistoryCommand(VEconomy instance)
    {
        super(null, "voyagecore.veconomy.player.bank.userhistory", "View the banks user history (promotions, demotions, etc)", true, "userhistory");
        this.instance = instance;

        instance.getInstance().getMainConfig().populate(this);
        addArguments(new ArgumentField("bank name", true), new ArgumentField("date (yyyy/mm/dd)", false));
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        UUID id = null;
        Player p = (Player) sender;
        VEconomyPlayer player = instance.get(p);
        String[] split = arguments.get(0).split("/");

        if (split.length == 1) {
            List<UUID> bankIds = player.getSharedAccounts();

            if (bankIds.size() == 0) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank};" + arguments.get(0))));
                return;
            }

            if (bankIds.size() > 1) {
                sender.sendMessage(Format.colour(Format.format(specifyBankOwnerMessage, "{amount};" + String.valueOf(bankIds.size()))));
                return;
            }

            id = bankIds.get(0);
        } else if (split.length == 2) {
            Optional<PlayerProfile> targetProfile = instance.getInstance().getBackupLookup().lookup(split[0]);

            if (!targetProfile.isPresent()) {
                sender.sendMessage(Format.colour(Format.format(playerNotFoundMessage, "{player};" + split[0])));
                return;
            }

            VEconomyPlayer targetPlayer = instance.get(targetProfile.get().getId());

            UUID account = targetPlayer.getSharedAccounts().stream().filter(id1 -> instance.getAccount(id1).getName().equalsIgnoreCase(split[1])).findFirst().orElse(null);

            if (account == null) {
                sender.sendMessage(Format.colour(Format.format(bankNotFoundMessage, "{bank];" + arguments.get(0))));
                return;
            }

            id = account;
        }

        SharedAccount account = instance.getAccount(id);

        if (!account.isMember(p.getUniqueId()) || (!account.getMembers().get(p.getUniqueId()).equals(SharedAccount.Type.POA) && !account.getOwner().equals(p.getUniqueId())) || !account.getOwner().equals(p.getUniqueId())) {
            sender.sendMessage(Format.colour(noPermissionMessage));
            return;
        }

        if (arguments.size() == 1) {
            instance.getHandler().getEntireHistory(id).whenCompleteAsync((entries, throwable) -> {

                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(noEntriesMessage));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();
                message.add(Format.colour(histroyHeader));

                entries.forEach(entry -> {
                    Optional<PlayerProfile> targetPlayer = instance.getInstance().getBackupLookup().lookup(entry.getMember());
                    String name;

                    if (!targetPlayer.isPresent()) {
                        name = "Console";
                    } else {
                        name = targetPlayer.get().getName();
                    }

                    String messageEntry = null;

                    if (entry.getAction().equals(Action.INVITED_MEMBER)) {
                        String invitedPlayer = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("invitedMember"))).get().getName();
                        messageEntry = Format.format(inviteEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{player};" + invitedPlayer);
                    }

                    if (entry.getAction().equals(Action.REMOVE_MEMBER)) {
                        String removedPlayer = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("invitedMember"))).get().getName();
                        messageEntry = Format.format(removedEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{player};" + removedPlayer);
                    }

                    if (entry.getAction().equals(Action.TRANSFER_OWNERSHIP)) {
                        String newOwner = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("transferredOwnershipTp"))).get().getName();
                        messageEntry = Format.format(ownershipTransferredEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{newowner};" + newOwner);
                    }

                    if (entry.getAction().equals(Action.LEAVE_BANK)) {
                        messageEntry = Format.format(leftEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                    }

                    if (entry.getAction().equals(Action.MEMBERSHIP_DENIED)) {
                        String requester = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("requester"))).get().getName();
                        messageEntry = Format.format(inviteRejectedEntry, "{player};" + name, "{requester};" + requester, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                    }

                    if (entry.getAction().equals(Action.MEMBERSHIP_ACCEPTED)) {
                        String requester = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("requester"))).get().getName();
                        String member = instance.getInstance().getBackupLookup().lookup(entry.getMember()).get().getName();
                        messageEntry = Format.format(inviteAcceptedEntry, "{player};" + member, "{requester};" + requester, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                    }

                    if (messageEntry == null) {
                        instance.getLogger().info("Message entry was null.");
                        return;
                    }
                    message.add(Format.colour(messageEntry));
                });

                message.add(Format.colour(historyFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        } else if (arguments.size() == 2) {
            Date date;

            try {
                date = dateFormat.parse(arguments.get(1));
            } catch (ParseException e) {
                sender.sendMessage(Format.colour(incorrectDateFormatMessage));
                return;
            }

            instance.getHandler().getHistroyFrom(id, date).whenCompleteAsync((entries, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }

                if (entries.size() == 0) {
                    sender.sendMessage(Format.colour(noEntriesMessage));
                    return;
                }

                LinkedList<String> message = Lists.newLinkedList();

                message.add(Format.colour(histroyHeader));

                for (HistoryEntry entry : entries) {
                    Optional<PlayerProfile> targetPlayer = instance.getInstance().getBackupLookup().lookup(entry.getMember());
                    String name;

                    if (!targetPlayer.isPresent()) {
                        name = "Console";
                    } else {
                        name = targetPlayer.get().getName();
                    }

                    String messageEntry = null;

                    switch (entry.getAction()) {
                        case INVITED_MEMBER:
                            String invitedPlayer = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("invitedMember"))).get().getName();
                            messageEntry = Format.format(inviteEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{player};" + invitedPlayer);
                            break;
                        case REMOVE_MEMBER:
                            String removedPlayer = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("invitedMember"))).get().getName();
                            messageEntry = Format.format(removedEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{player};" + removedPlayer);
                            break;
                        case TRANSFER_OWNERSHIP:
                            String newOwner = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("transferredOwnershipTp"))).get().getName();
                            messageEntry = Format.format(ownershipTransferredEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()), "{newowner};" + newOwner);
                            break;
                        case LEAVE_BANK:
                            messageEntry = Format.format(leftEntry, "{member};" + name, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                            break;
                        case MEMBERSHIP_DENIED:
                            String requestor = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("requester"))).get().getName();
                            messageEntry = Format.format(inviteRejectedEntry, "{player};" + name, "{requester};" + requestor, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                            break;
                        case MEMBERSHIP_ACCEPTED:
                            String requester = instance.getInstance().getBackupLookup().lookup(UUID.fromString((String) entry.getData().get("requester"))).get().getName();
                            messageEntry = Format.format(inviteAcceptedEntry, "{player};" + name, "{requester};" + requester, "{date};" + dateFormat.format(entry.getDate()), "{time};" + timeFormat.format(entry.getDate()));
                    }

                    if (messageEntry == null) {
                        continue;
                    }

                    message.add(Format.colour(messageEntry));

                }

                message.add(Format.colour(historyFooter));
                sender.sendMessage(message.toArray(new String[0]));
            });
        }
    }
}
