package cm.pvp.voyagepvp.voyagecore.api.tasks;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.exception.UtilityException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Convenience methods for accessing the Bukkit scheduler.
 */
public class Tasks
{
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    private Tasks()
    {
        throw new UtilityException();
    }

    public static int runSync(Runnable runnable)
    {
        return Tasks.scheduler.runTask(VoyageCore.get(), runnable).getTaskId();
    }

    public static int runSyncLater(Runnable runnable, long ticks)
    {
        return Tasks.scheduler.runTaskLater(VoyageCore.get(), runnable, ticks).getTaskId();
    }

    public static int runSyncRepeating(Runnable runnable, long tickRepeat)
    {
        return Tasks.runSyncLaterRepeating(runnable, 0, tickRepeat);
    }

    public static int runSyncLaterRepeating(Runnable runnable, long later, long tickRepeat)
    {
        return Tasks.scheduler.scheduleSyncRepeatingTask(VoyageCore.get(), runnable, later, tickRepeat);
    }

    public static int runAsync(Runnable runnable)
    {
        return Tasks.scheduler.runTaskAsynchronously(VoyageCore.get(), runnable).getTaskId();
    }

    public static int runAsyncLater(Runnable runnable, long ticks)
    {
        return Tasks.runSyncLater(() -> Tasks.runAsync(runnable), ticks);
    }

    public static int runAsyncRepeating(Runnable runnable, long tickRepeat)
    {
        return Tasks.runAsyncLaterRepeating(runnable, 0, tickRepeat);
    }

    public static int runAsyncLaterRepeating(Runnable runnable, long later, long tickRepeat)
    {
        return Tasks.runSyncLaterRepeating(() -> Tasks.runAsync(runnable), later, tickRepeat);
    }

    public static int runLaterRepeating(Runnable runnable, boolean sync, long later, long tickRepeat)
    {
        return sync ? Tasks.runSyncLaterRepeating(runnable, later, tickRepeat) : Tasks.runAsyncLaterRepeating(runnable, later, tickRepeat);
    }

    public static void cancel(int id) {
        Tasks.scheduler.cancelTask(id);
    }
}

