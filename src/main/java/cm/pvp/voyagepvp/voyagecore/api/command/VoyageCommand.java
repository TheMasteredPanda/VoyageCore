package cm.pvp.voyagepvp.voyagecore.api.command;

import cm.pvp.voyagepvp.voyagecore.api.command.argument.ArgumentField;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.command.locale.DefaultCommandLocale;
import cm.pvp.voyagepvp.voyagecore.api.exception.CommandException;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Cacheable;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import javax.naming.OperationNotSupportedException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static cm.pvp.voyagepvp.voyagecore.api.command.locale.CommandLocale.Key.*;


/**
 * A command wrapper. It serves as a more advanced command executor.
 * This wrapper can determine if each argument for a command is the correct
 * data type, if the player has the correct permission, whether an alias of
 * the command has been invoked, and gives the command sender, when invoked,
 * a detailed json list of the command and it's child commands.
 */
@Getter
public abstract class VoyageCommand extends BukkitCommand
{
    private boolean playerOnlyCommand;
    private VoyageCommand parent;
    private LinkedList<VoyageCommand> children = Lists.newLinkedList();
    private LinkedList<ArgumentField> fields = Lists.newLinkedList();
    private CommandLocale locale;

    public VoyageCommand(CommandLocale locale, String permission, String description, boolean playerOnlyCommand, String... aliases)
    {
        super(aliases[0]);
        List<String> sortedAliases = Lists.newArrayList(aliases);
        sortedAliases.remove(0);
        if (sortedAliases.size() != 0) setAliases(sortedAliases);
        setDescription(description);
        setPermission(permission);
        this.locale = locale == null ? new DefaultCommandLocale() : locale;
        setPermissionMessage(this.locale.get(NO_PERMISSION));
        this.playerOnlyCommand = playerOnlyCommand;
    }

    /**
     * Add arguments to this command. Beware, a required command can not come after an
     * optional argument. However, an optional argument can come after a required argument.
     * @param fields - the immutable array of arguments.
     * @throws OperationNotSupportedException
     */
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

    /**
     * Set the parent of this command, making it a child of that command.
     * @param parent - command to set the parent to.
     */
    public final void setParent(VoyageCommand parent)
    {
        parent.addChildren(this);
        this.parent = parent;
    }

    /**
     * Get the parent command.
     * @return
     */
    public final VoyageCommand getParent()
    {
        return parent;
    }

    /**
     * Add commands as children (sub commands) of this command.
     * @param children - immutable array of child commands.
     */
    public final void addChildren(VoyageCommand... children)
    {
        for (VoyageCommand child : children) {
            if (child.getParent() != null) {
                throw new CommandException("Command " + child.getCommandPath() + " already has a parent.");
            }

            if (this.children.contains(child)) {
                continue;
            }

            this.children.add(child);
            child.setParent(this);
        }
    }

    @Override
    public final boolean execute(CommandSender sender, String label, String[] args)
    {
        if (isPlayerOnlyCommand() && !(sender instanceof Player)) {
            sender.sendMessage(Format.colour(locale.get(PLAYER_ONLY_COMMAND)));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            return true;
        }

        LinkedList<String> arguments = Lists.newLinkedList(Arrays.asList(args));

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                //TODO help command, displaying all commands that seem this command a parent.
                return true;
            }

            for (VoyageCommand child : children) {
                if (!child.isAlias(args[0])) {
                    continue;
                }

                arguments.remove(0);
                return child.execute(sender, args[0], arguments.toArray(new String[0]));
            }
        }

        if (getRequiredFields().size() > arguments.size()) {
            sender.sendMessage(Format.colour(locale.get(NOT_ENOUGH_ARGUMENTS)));
            return true;
        }

        if (args.length > 0) {
            for (int i = 0; i < fields.size(); i++) {
                ArgumentField field = fields.get(i);

                if (!field.getCheckFunction().check(args[i])) {
                    sender.sendMessage(Format.format(locale.get(ARGUMENT_INCORRECT).replace("{argument}", field.getName())));
                    return true;
                }
            }
        }

        execute(sender, this, arguments);
        return true;
    }

    /**
     * Get the command usage. This pertains the command path plus the arguments the command has.
     * @return the command usage.
     */
    public String getCommandUsage()
    {
        StringBuilder sb = new StringBuilder(getCommandPath() + " ");

        for (ArgumentField field : fields) {
            sb.append(field.isRequired() ? "[" : "<").append(field.getName()).append(field.isRequired() ? "]" : ">");

            if (field != fields.getLast()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Get the full command path of this command.
     * @return the command path.
     */
    public String getCommandPath()
    {
        return (getParent() != null ? getParent().getCommandPath() + " " : "/") + getName();
    }

    /**
     * Gets the list of required argument fields.
     * @return
     */
    @Cacheable
    public LinkedList<ArgumentField> getRequiredFields()
    {
        return fields.stream().filter(ArgumentField::isRequired).collect(Collectors.toCollection(Lists::newLinkedList));
    }

    /**
     * Cheeck if a string is an alias of this command.
     * @param alias - alias to check.
     * @return true if it is, else false.
     */
    public boolean isAlias(String alias)
    {
        return getAliases().contains(alias) || getName().equals(alias);
    }

    /**
     * The method signature that will be inherited then defined.
     * @param sender - the person who sent the command.
     * @param command - the command executed.
     * @param arguments - the arguments.
     */
    public abstract void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments);
}
