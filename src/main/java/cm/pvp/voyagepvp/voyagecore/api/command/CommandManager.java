package cm.pvp.voyagepvp.voyagecore.api.command;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.manager.Manager;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager extends Manager<VoyageCore>
{
    private CommandManager(VoyageCore instance)
    {
        super(instance, "Command Manager", 1.0);
    }

    public static void addCommands(JavaPlugin instance, Command command)
    {
        
    }
}
