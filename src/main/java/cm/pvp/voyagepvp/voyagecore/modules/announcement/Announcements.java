package cm.pvp.voyagepvp.voyagecore.modules.announcement;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.modules.announcement.commands.AnnouncementCommand;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.LinkedList;
import java.util.Map;

public class Announcements extends Feature
{

    @Getter
    private LinkedList<BaseComponent[]> announcements = Lists.newLinkedList();

    @Getter @Setter
    private AnnouncementThread announcementThread;

    public Announcements(VoyageCore instance)
    {
        super(instance, "Announcements", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new AnnouncementCommand(getInstance(), this));

        Map<String, Object> announcements = getSection().getConfigurationSection("announcements").getValues(true);


        for (Map.Entry<String, Object> entry : announcements.entrySet()) {
            this.announcements.add(ComponentSerializer.parse(Format.colour((String) entry.getValue())));
        }

        if (this.announcements.size() != 0) {
            announcementThread = new AnnouncementThread(getInstance(), this.announcements, getSection().getBoolean("random"), getSection().getInt("interval") * 20);
        }
        return true;
    }

    @Override
    protected void disable()
    {
        if (announcementThread != null) {
            announcementThread.cancel();
            announcementThread = null;
        }
    }
}
