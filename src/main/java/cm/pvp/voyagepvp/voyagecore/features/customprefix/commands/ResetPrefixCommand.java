package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class ResetPrefixCommand extends VoyageCommand
{
    private CustomPrefix feature;

    @ConfigPopulate("features.customprefix.prefixreset")
    private String prefixReset;

    @ConfigPopulate("features.customprefix.nocustomprefix")
    private String noCustomPrefix;

    public ResetPrefixCommand(CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.reset", "Remove the custom prefix", true, "reset");
        this.feature = feature;
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        UUID id = ((Player) sender).getUniqueId();

        if (feature.getHandler().exists(id)) {
            User user = feature.getApi().getUser(id);
            Node node = feature.getApi().buildNode("prefix.9000." + feature.getHandler().get(id).get()).build();

            Objects.requireNonNull(user).unsetPermission(node);
            feature.getHandler().remove(id);
            sender.sendMessage(Format.colour(prefixReset));
        } else {
            sender.sendMessage(Format.colour(noCustomPrefix));
        }
    }
}
