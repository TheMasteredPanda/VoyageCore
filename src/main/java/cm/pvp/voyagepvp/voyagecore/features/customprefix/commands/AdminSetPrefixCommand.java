package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.argument.check.PlayerCheckFunction;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.naming.OperationNotSupportedException;
import java.util.LinkedList;
import java.util.List;

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

    @ConfigPopulate("Features.customprefix.blacklist")
    private List<String> blacklist;

    @ConfigPopulate("features.customprefix.messages.containsblacklistedword")
    private String containsBlacklistedWord;

    public AdminSetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.admin.set", "Set a players prefix", false, "set");
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
        String player = arguments.get(0);
        arguments.remove(0);
        String prefix = Joiner.on(' ').join(arguments).replaceAll("(&)\\w", "");

        if (prefix.length() > prefixLength) {
            sender.sendMessage(Format.colour(Format.format(prefixTooLong, "{prefix};" + prefix, "{length};" + String.valueOf(prefixLength))));
            return;
        }

        for (String word : blacklist) {
            if (prefix.toLowerCase().contains(word.toLowerCase())) {
                sender.sendMessage(Format.colour(Format.format(containsBlacklistedWord, "{word};" + word)));
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player + " meta removeprefix 9000");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player + "meta addprefix 9000 " + arguments.get(0));
        sender.sendMessage(Format.colour(Format.format(prefixSetFor, "{player};" + player, "{prefix};" + arguments.get(1))));
    }
}
