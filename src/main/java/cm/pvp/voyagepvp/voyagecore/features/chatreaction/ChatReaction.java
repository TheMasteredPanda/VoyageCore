package cm.pvp.voyagepvp.voyagecore.features.chatreaction;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.features.chatreaction.commands.StatsCommand;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

@Getter
public class ChatReaction extends Feature
{
    private ArrayList<String> words = Lists.newArrayList();

    @Getter
    private DataHandler handler;

    @Setter
    private ScrambleBroadcasterThread scambleThread = null;

    @Setter
    private CountdownThread countdown;

    public ChatReaction()
    {
        super("ChatReaction", 1.0);

        File wordFile = new File(getInstance().getDataFolder() + File.separator + "chatreaction-words.txt");

        if (wordFile.exists()) {
            try {
                Files.lines(wordFile.toPath()).forEach(line -> words.add(line));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean enable() throws Exception
    {
        if (words.size() == 0) {
            getLogger().warning("If you want ChatReaction to be enabled, fill a file with words, separated one per line, in a file named 'chatreaction-words.txt'");
            return false;
        }

        handler = new DataHandler(getInstance(), this);
        scambleThread = new ScrambleBroadcasterThread(getInstance(), this);
        getInstance().register(new StatsCommand(getInstance(), handler));
        return true;
    }

    @Override
    protected void disable() throws Exception
    {
        if (handler != null) {
            handler.close();
        }
    }
}
