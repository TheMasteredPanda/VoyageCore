package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class ScrambleBroadcasterThread extends BukkitRunnable
{
    private ChatReaction feature;
    private VoyageCore instance;
    private final Random r = new Random();
    private String broadcast;

    public ScrambleBroadcasterThread(VoyageCore instance, ChatReaction feature)
    {
        if (feature.getCountdown() != null) feature.getCountdown().cancel();;
        feature.setCountdown(null);

        this.instance = instance;
        this.feature = feature;
        broadcast = feature.getSection().getString("messages.broadcast");
        runTaskTimerAsynchronously(instance, 0L, feature.getSection().getInt("countdown") * 20);
    }

    @Override
    public void run()
    {
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
        TextComponent component = new TextComponent(broadcast.split("\\{randomword}")[0]);
        TextComponent randomWord = new TextComponent(shuffledWord);
        randomWord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(chosenWord)));
        component.addExtra(component);
        component.addExtra(broadcast.split("\\{randomword}")[1]);
        Bukkit.spigot().broadcast(component);
        feature.setCountdown(new CountdownThread(instance, feature, new Pair<>(chosenWord, shuffledWord)));
    }
}
