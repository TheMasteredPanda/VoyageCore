package cm.pvp.voyagepvp.voyagecore.api.player;

import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public final class Players
{
    private Players()
    {
        throw new UtilityException();
    }

    public static Player get(String name)
    {
        return Bukkit.getPlayer(name);
    }

    public static Player get(UUID id)
    {
        return Bukkit.getPlayer(id);
    }

    public static Collection<? extends Player> online()
    {
        return Bukkit.getOnlinePlayers();
    }

    public static boolean online(UUID id)
    {
        return get(id).isOnline();
    }
}
