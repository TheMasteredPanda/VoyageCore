package cm.pvp.voyagepvp.voyagecore.api.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

@Getter
public class Command implements CommandExecutor
{
    private ImmutableList<String> aliases;
    private String permission;
    private String description;
    private boolean playerOnlyCommand = false;
    private Command parent;
    private LinkedList<Command> children = Lists.newLinkedList();

    public Command(String permission, String description, boolean playerOnlyCommand, String... aliases)
    {
        this.permission = permission;
        this.description = description;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String s, String[] args)
    {
        return false;
    }

    public boolean isAlias(String alias)
    {
        return aliases.contains(alias);
    }

    public String getMainAlias()
    {
        return aliases.get(0);
    }


}
