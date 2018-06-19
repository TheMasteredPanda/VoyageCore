package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class SetPrefixCommand extends VoyageCommand
{
    private CustomPrefix feature;
    private VoyageCore instance;

    @ConfigPopulate("features.customprefix.messages.setprefix")
    private String setPrefix;

    @ConfigPopulate("features.customprefix.messages.prefixtoolong")
    private String prefixTooLong;


    @ConfigPopulate("features.customprefix.length")
    private int prefixLength;

    public SetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.setprefix", "Set a custom prefix.", true, "set");
        this.instance = instance;
        this.feature = feature;

        try {
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        String prefix = arguments.get(0).replaceAll("(&)\\w", "");

        if (prefix.length() > prefixLength) {
            sender.sendMessage(Format.colour(Format.format(prefixTooLong, "{prefix};" + arguments.get(0), "{length};" + String.valueOf(prefixLength))));
            return;
        }

        UUID id = ((Player) sender).getUniqueId();
        User user = feature.getApi().getUser(id);
        String permission = "prefix.9000." + arguments.get(0);
        Node node = feature.getApi().buildNode(permission).build();
        Objects.requireNonNull(user).setPermission(node);

        if (feature.getHandler().exists(id)) {
            feature.getHandler().update(id, permission);
        } else {
            feature.getHandler().add(id, permission);
        }
    }
}
