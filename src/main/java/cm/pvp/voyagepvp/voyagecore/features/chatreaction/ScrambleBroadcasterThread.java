package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class ScrambleBroadcasterThread extends BukkitRunnable
{
    private ChatReaction feature;
    private VoyageCore instance;
    private final Random r = new Random();
    private int cooldown;

    public ScrambleBroadcasterThread(VoyageCore instance, ChatReaction feature)
    {
        if (feature.getCountdown() != null) feature.getCountdown().cancel();;
        feature.setCountdown(null);

        this.instance = instance;
        this.feature = feature;
        cooldown = feature.getSection().getInt("cooldown");
        System.out.println("Initial cooldown number: " + cooldown);
        runTaskTimerAsynchronously(instance, 0L, 20);
    }

    @Override
    public void run()
    {
        System.out.println("Cooldown: " + cooldown);

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        String chosenWord = feature.getWords().get(r.nextInt(feature.getWords().size() - 1));

        LinkedList<Character> chars = Lists.newLinkedList();

        for ( char c : chosenWord.toCharArray() ) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        char[] shuffled = new char[chars.size()];
        for ( int i = 0; i < shuffled.length; i++ ) {
            shuffled[i] = chars.get(i);
        }

        String shuffledWord = new String(shuffled);
        feature.setCountdown(new CountdownThread(instance, feature, new Pair<>(chosenWord, shuffledWord)));
    }
}
