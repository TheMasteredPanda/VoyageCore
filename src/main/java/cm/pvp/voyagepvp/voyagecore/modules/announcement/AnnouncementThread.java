package cm.pvp.voyagepvp.voyagecore.modules.announcement;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Random;

public class AnnouncementThread extends BukkitRunnable
{
    private LinkedList<BaseComponent[]> announcements;
    private Random dice;
    private int last = -1;

    public AnnouncementThread(VoyageCore instance, LinkedList<BaseComponent[]> announcements, boolean random, int interval)
    {
        this.announcements = announcements;


        if (random) {
            dice = new Random();
        }

        runTaskTimerAsynchronously(instance, 0, interval);
    }

    @Override
    public void run()
    {
        if (dice != null) {
            int chosen = dice.nextInt(announcements.size() - 1);

            while (chosen != last) {
                last = chosen;
            }
        } else {
            last = last + 1;

            if ((announcements.size() - 1) < last) {
                last = 0;
            }


            Bukkit.spigot().broadcast(announcements.get(last));
        }
    }
}
