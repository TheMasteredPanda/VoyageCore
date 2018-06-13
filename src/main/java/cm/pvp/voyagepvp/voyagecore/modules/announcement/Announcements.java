package cm.pvp.voyagepvp.voyagecore.modules.announcement;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Announcements extends Feature
{
    private LinkedList<TextComponent> announcements = Lists.newLinkedList();
    private final Pattern placeholder = Pattern.compile("[{(<](\\w*);(\\w*)[})>]");

    @Getter
    private AnnouncementThread announcementThread;

    public Announcements(VoyageCore instance)
    {
        super(instance, "Announcements", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        Map<String, Object> announcements = getSection().getConfigurationSection("announcements").getValues(true);

        for (Map.Entry<String, Object> entry : announcements.entrySet()) {
            List<String> text = (List<String>) entry.getValue();
            TextComponent announcement = new TextComponent();


            for (String line : text) {
                String[] words = line.split(" ");

                for (String word : words) {
                    Matcher m = placeholder.matcher(word);

                    if (!m.matches()) {
                        announcement.addExtra(new TextComponent(word + " "));
                        continue;
                    }

                    StringBuilder sb = new StringBuilder(word);
                    sb.deleteCharAt(0);
                    sb.deleteCharAt(word.length() - 1);
                    String[] pair = sb.toString().split(";");
                    TextComponent component = new TextComponent(pair[0]);

                    if (word.startsWith("(")) { //OPEN URL
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, pair[1]));
                    }

                    if (word.startsWith("{")) {
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, pair[1]));
                    }

                    if (word.startsWith("<")) {
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, pair[1]));
                    }

                    announcement.addExtra(component);


                    if (!line.equals(text.get(text.size() - 1))) {
                        announcement.addExtra(" ");
                    }
                }

                announcement.addExtra("\n");
            }

            this.announcements.add(announcement);
        }


        if (announcements.size() != 0) {
            announcementThread = new AnnouncementThread(getInstance(), this.announcements, getSection().getBoolean("random"), getSection().getInt("internal"));
        }

        return true;
    }
}
