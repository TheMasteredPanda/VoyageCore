package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import me.lucko.luckperms.api.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class AdminSetPrefixCommand extends VoyageCommand
{
    private VoyageCore instance;
    private CustomPrefix feature;

    @ConfigPopulate("features.customprefix.messages.prefixsetfor")
    private String prefixSetFor;

    @ConfigPopulate("features.customprefix.messages.prefixtoolong")
    private String prefixTooLong;


    @ConfigPopulate("features.customprefix.length")
    private int prefixLength;

    public AdminSetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.admin.set", "Set a players prefix", false, "aset");
        this.instance = instance;
        this.feature = feature;

        try {
            ArgumentField field = new ArgumentField("player name", true);
            field.setCheckFunction(new PlayerCheckFunction(instance.getMojangLookup()));
            addArguments(field, new ArgumentField("prefix", true));
            instance.getMainConfig().populate(this);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {

        String prefix = arguments.get(1).replaceAll("(&)\\w", "");

        if (prefix.length() > prefixLength) {
            sender.sendMessage(Format.colour(Format.format(prefixTooLong, "{prefix};" + arguments.get(0), "{length};" + String.valueOf(prefixLength))));
            return;
        }

        UUID target = Bukkit.getPlayer(arguments.get(1)) == null ? instance.getMojangLookup().lookup(arguments.get(1)).get().getId() : Bukkit.getPlayer(arguments.get(1)).getUniqueId();

        User user = feature.getApi().getUser(target);
        String node = "prefix.9000." + arguments.get(1);


        if (feature.getHandler().exists(target)) {
            Objects.requireNonNull(user).unsetPermission(feature.getApi().buildNode(feature.getHandler().get(target).get()).build());
            feature.getHandler().update(target, node);
        } else {
            feature.getHandler().add(target, node);
            Objects.requireNonNull(user).setPermission(feature.getApi().buildNode(node).build());
        }

        sender.sendMessage(Format.colour(Format.format(prefixSetFor, "{player};" + arguments.get(0), "{prefix};" + arguments.get(1))));
    }
}
