package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomyPlayer;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.shared.SharedAccount;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class BankListCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("features.veconomy.messages.bank.list.header")
    private String header;

    @ConfigPopulate("features.veconomy.messages.bank.list.footer")
    private String footer;

    @ConfigPopulate("features.veconomy.messages.bank.list.entry")
    private String bankEntry;

    @ConfigPopulate("features.veconomy.messages.bank.list.entryinformationtemplate")
    private String informationTemplate;

    @ConfigPopulate("features.veconomy.messages.bank.list.nobankstolist")
    private String noBanksToListMessage;

    @ConfigPopulate("features.veconomy.messages.bank.list.memberentry")
    private String memberEntry;

    public BankListCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.list", "Lists the banks accessible to you.", true, "list", "l");
        this.feature = feature;

        try {
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

        if (player.getSharedAccounts().size() == 0) {
            sender.sendMessage(Format.colour(noBanksToListMessage));
            return;
        }

        TextComponent component = new TextComponent();

        component.addExtra(Format.colour(header));

        for (UUID bank : player.getSharedAccounts()) {
            SharedAccount account = feature.getAccount(bank);

            TextComponent entry = new TextComponent(Format.colour(Format.format(bankEntry, "{name};" + account.getName())));

            LinkedList<String> members = Lists.newLinkedList();

            for (Map.Entry<UUID, SharedAccount.Type> member : account.getMembers().entrySet()) {
                PlayerProfile profile = feature.getInstance().getMojangLookup().lookup(member.getKey()).orElse(null);
                String name;

                if (profile == null) {
                    name = Bukkit.getPlayer(member.getKey()).getName();
                } else {
                    name = profile.getName();
                }

                members.add(Format.colour(Format.format(memberEntry, "{name};" + name, "{rank};" + WordUtils.capitalize(member.getValue().name().toLowerCase()))));
            }

            String filled = Format.colour(Format.format(informationTemplate, "{balance};" + feature.getVaultHook().format(account.getBalance()), "{members};" + Joiner.on(", ").join(members)));
            entry.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(filled)));
            component.addExtra(entry);
        }

        component.addExtra(Format.colour(footer));
        p.spigot().sendMessage(component);
    }
}
