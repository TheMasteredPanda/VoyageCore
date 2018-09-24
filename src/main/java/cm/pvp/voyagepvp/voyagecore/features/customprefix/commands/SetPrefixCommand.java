package cm.pvp.voyagepvp.voyagecore.features.customprefix.commands;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.features.customprefix.CustomPrefix;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;

public class SetPrefixCommand extends VoyageCommand
{
    private CustomPrefix feature;


    @ConfigPopulate("features.customprefix.messages.setprefix")
    private String setPrefix;

    @ConfigPopulate("features.customprefix.messages.prefixtoolong")
    private String prefixTooLong;

    @ConfigPopulate("features.customprefix.length")
    private int prefixLength;

    @ConfigPopulate("features.customprefix.messages.containsblacklistedword")
    private String containsBlacklistedWord;

    @ConfigPopulate("features.customprefix.blacklist")
    private List<String> blacklist;

    public SetPrefixCommand(VoyageCore instance, CustomPrefix feature)
    {
        super(null, "voyagecore.customprefix.setprefix", "Set a custom prefix.", true, "set");
        this.feature = feature;

        instance.getMainConfig().populate(this);
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        System.out.println("Executed command.");
        String fullPrefix = Joiner.on(' ').join(arguments);
        String prefix = fullPrefix.replaceAll("(&)\\w", "").replace("\"", "");

        if (prefix.length() > prefixLength) {
            sender.sendMessage(Format.colour(Format.format(prefixTooLong, "{prefix};" + fullPrefix, "{length};" + String.valueOf(prefixLength))));
            return;
        }

        for (String word : blacklist) {
            if (prefix.toLowerCase().contains(word.toLowerCase())) {
                sender.sendMessage(Format.colour(Format.format(containsBlacklistedWord, "{word};" + word)));
                return;
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta removeprefix 9000");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + sender.getName() + " meta addprefix 9000 " + fullPrefix);
        sender.sendMessage(Format.colour(Format.format(setPrefix, "{prefix};" + fullPrefix)));
    }
}
