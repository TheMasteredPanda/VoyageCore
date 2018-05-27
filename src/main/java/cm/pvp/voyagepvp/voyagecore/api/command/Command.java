package cm.pvp.voyagepvp.voyagecore.api.command;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.DefaultCommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Cacheable;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale.Key.*;

@Getter
public abstract class Command implements CommandExecutor
{
    private ImmutableList<String> aliases;
    private String permission;
    private String description;
    private boolean playerOnlyCommand = false;
    private Command parent;
    private LinkedList<Command> children = Lists.newLinkedList();
    private LinkedList<ArgumentField> fields = Lists.newLinkedList();
    private CommandLocale locale;

    public Command(CommandLocale locale, String permission, String description, boolean playerOnlyCommand, String... aliases)
    {
        this.permission = permission;
        this.description = description;
        this.locale = locale == null ? new DefaultCommandLocale() : locale;
        this.aliases = ImmutableList.<String>builder().addAll(Arrays.asList(aliases)).build();
    }

    public void addArguments(ArgumentField... fields) throws OperationNotSupportedException
    {
        boolean required = false;

        for (ArgumentField field : fields) {
            if (required && field.isRequired()) {
                throw new OperationNotSupportedException("Cannot have a required argument field after a non-required argument field.");
            } else {
                required = field.isRequired();
            }
        }

        this.fields.addAll(Arrays.asList(fields));
    }

    public void setParent(Command parent)
    {
        this.parent = parent;
    }

    public Command getParent()
    {
        return parent;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
    {
        if (isPlayerOnlyCommand() && !(sender instanceof Player)) {
            sender.sendMessage(Format.colour(locale.get(PLAYER_ONLY_COMMAND)));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Format.colour(locale.get(NO_PERMISSION)));
            return true;
        }

        LinkedList<String> arguments = Lists.newLinkedList(Arrays.asList(args));

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                //TODO help command, displaying all commands that seem this command a parent.
                return true;
            }

            for (Command child : children) {
                if (!child.isAlias(args[1])) {
                    continue;
                }

                arguments.remove(0);
                onCommand(sender, cmd, label, arguments.toArray(new String[0]));
                return true;
            }
        }

        if (getRequiredFields().size() > arguments.size()) {
            sender.sendMessage(locale.get(NOT_ENOUGH_ARGUMENTS));
            return true;
        }



        for (ArgumentField field : fields) {

        }

        execute(sender, this, Lists.newLinkedList()); /*TODO arguments*/

        return false;
    }


    @Cacheable
    public LinkedList<ArgumentField> getRequiredFields()
    {
        return fields.stream().filter(ArgumentField::isRequired).collect(Collectors.toCollection(Lists::newLinkedList));
    }

    public boolean isAlias(String alias)
    {
        return aliases.contains(alias);
    }

    public String getMainAlias()
    {
        return aliases.get(0);
    }

    public abstract void execute(CommandSender sender, Command command, LinkedList<String> arguments);
}
