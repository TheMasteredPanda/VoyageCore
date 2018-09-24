package cm.pvp.voyagepvp.voyagecore.features.customlist;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import cm.pvp.voyagepvp.voyagecore.api.config.wrapper.ConfigPopulate;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.player.Players;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vexsoftware.votifier.google.common.collect.ArrayListMultimap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomList extends Feature
{
    @ConfigPopulate("features.customlist.messages.header")
    private String header;

    @ConfigPopulate("features.customlist.messages.footer")
    private String footer;

    @ConfigPopulate("features.customlist.messages.entry")
    private String listEntry;

    @ConfigPopulate("features.customlist.messages.onlineplayercount")
    private String onlinePlayerCount;

    @ConfigPopulate("features.customlist.format")
    private List<String> format;

    private HashMap<String, GroupEntry> roleGroups = Maps.newHashMap();

    private LuckPermsApi api;

    public CustomList(VoyageCore instance)
    {
        super(instance, "CustomList", 1.0);

        getInstance().getMainConfig().populate(this);
    }

    @Override
    protected boolean enable() throws Exception
    {
        RegisteredServiceProvider<LuckPermsApi> luckPerms = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);

        if (luckPerms == null) {
            return false;
        }

        api = luckPerms.getProvider();

        for (String key : getSection().getConfigurationSection("groups").getKeys(true)) {
            String name = getSection().getString("groups." + key + ".name");
            List<String> roles = getSection().getStringList("groups." + key + ".roles");
            roleGroups.put(key, new GroupEntry(key, name, roles));
        }

        getInstance().register(new ListCommand());
        return true;
    }

    public class ListCommand extends VoyageCommand
    {
        public ListCommand()
        {
            super(null, "voyagecore.customlist", "Shows the custom online list.", true, "list");
        }

        @Override
        public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
        {
            List<String> formatCopy = Lists.newArrayList(format);

            formatCopy.replaceAll(s -> {
                s = Format.format(s, "{header};" + header, "{footer};" + footer, "{onlineplayercount};" + Format.format(onlinePlayerCount, "{count};" + String.valueOf(Players.online().size())));
                String[] splitMessage = s.split(" ");

                for (String part : splitMessage) {
                    if (part.startsWith("{group:")) {
                        String id = part.replace("{group:", "").replace("}", "");

                        if (!roleGroups.containsKey(id)) {
                            getLogger().warning("Group id " + id + " isn't recognised.");
                            continue;
                        }

                        GroupEntry entry = roleGroups.get(id);
                        ArrayListMultimap<String, String> sortedGroups = ArrayListMultimap.create();

                        api.getUserManager().getLoadedUsers().forEach(user -> {
                            if (!entry.getRoles().contains(user.getPrimaryGroup().toLowerCase())) {
                                return;
                            }

                            sortedGroups.put(user.getPrimaryGroup().toLowerCase(), user.getName());
                        });

                        if (sortedGroups.size() == 0) {
                            s = s.replace(part, "");
                            continue;
                        }

                        String stringEntry = Format.colour(Format.format(listEntry, "{name};" + StringUtils.capitalize(entry.getName()), "{players};" + Joiner.on(", ").join(sortedGroups.values())));
                        s = Format.format(s, part + ";" + stringEntry);
                    }

                    if (part.startsWith("{role:")) {
                        String roleId = part.replace("{role:", "").replace("}", "");

                        Set<String> players = api.getUserManager().getLoadedUsers().stream().filter(user -> user.getPrimaryGroup().toLowerCase().equalsIgnoreCase(roleId)).map(User::getFriendlyName).collect(Collectors.toSet());

                        if (players.size() == 0) {
                            s = s.replace(part, "");
                            continue;
                        }

                        s = Format.format(s, part + ";" + Format.colour(Format.format(listEntry, "{name};" + StringUtils.capitalize(roleId), "{players};" + Joiner.on(", ").join(players))));
                    }
                }

                return Format.colour(s);
            });


            formatCopy.removeIf(s -> s.equalsIgnoreCase(""));

            sender.sendMessage(formatCopy.toArray(new String[0]));
        }
    }

    @Getter
    @AllArgsConstructor
    public class GroupEntry
    {
        private String id;
        private String name;
        private List<String> roles;
    }
}
