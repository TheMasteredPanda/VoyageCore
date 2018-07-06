package cm.pvp.voyagepvp.voyagecore.features.veconomy.commands.bank;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.lookup.PlayerProfile;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.VEconomy;
import cm.pvp.voyagepvp.voyagecore.features.veconomy.accounts.SharedAccount;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BankListCommand extends VoyageCommand
{
    private VEconomy feature;

    @ConfigPopulate("modules.veconomy.messages.bank.list.header")
    private String header;

    @ConfigPopulate("modules.veconomy.messages.bank.list.footer")
    private String footer;

    @ConfigPopulate("modules.veconomy.messages.bank.list.entry")
    private String bankEntry;

    @ConfigPopulate("modules.veconomy.messages.bank.list.entryinformationtemplate")
    private String informationTemplate;

    @ConfigPopulate("modules.veconomy.messages.bank.list.nobankstolist")
    private String noBanksToListMessage;

    public BankListCommand(VEconomy feature)
    {
        super(null, "voyagecore.veconomy.player.bank.list", "Lists the banks accessible to you.", true, "list", "l");
        this.feature = feature;
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        List<UUID> accessibleBanks = feature.getHandler().getAccessibleBanks(p.getUniqueId());

        if (accessibleBanks.size() == 0) {
            sender.sendMessage(Format.colour(noBanksToListMessage));
            return;
        }

        TextComponent component = new TextComponent();

        component.addExtra(Format.colour(header));

        for (UUID bank : accessibleBanks) {
            SharedAccount account = feature.getAccount(bank);

            TextComponent entry = new TextComponent(Format.format(bankEntry, "{name};" + account.getName()));

            LinkedList<String> members = Lists.newLinkedList();

            for (Map.Entry<UUID, SharedAccount.Type> member : account.getMembers().entrySet()) {
                PlayerProfile profile = feature.getInstance().getMojangLookup().lookup(member.getKey()).orElse(null);
                String name;

                if (profile == null) {
                    name = Bukkit.getPlayer(member.getKey()).getName();
                } else {
                    name = profile.getName();
                }

                members.add(name + ": " + member.getValue().name());
            }

            String filled = Format.colour(Format.format(informationTemplate, "{balance};" + String.valueOf(account.getBalance()), "{members};" + Joiner.on(", ").join(members)));
            entry.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(filled)));
            component.addExtra(entry);
        }

        component.addExtra(Format.colour(footer));
    }
}
