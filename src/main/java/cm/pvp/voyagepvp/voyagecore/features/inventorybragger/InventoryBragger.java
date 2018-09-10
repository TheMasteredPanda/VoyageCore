package cm.pvp.voyagepvp.voyagecore.features.inventorybragger;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.features.inventorybragger.command.InventoryBraggerCommand;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@Getter
public class InventoryBragger extends Feature implements Listener
{
    private ArrayListMultimap<UUID, UUID> requests = ArrayListMultimap.create();
    private HashMap<UUID, Inventory> viewing = Maps.newHashMap();


    public InventoryBragger(VoyageCore instance)
    {
        super(instance, "InventoryBragger", 1.0);
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new InventoryBraggerCommand(this));
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(InventoryClickEvent e)
    {
        Player p = (Player) e.getInventory().getHolder();

        if (!p.getUniqueId().equals(e.getWhoClicked().getUniqueId())) {
            return;
        }

        if (viewing.containsKey(e.getWhoClicked().getUniqueId()) && Objects.equals(e.getInventory(), viewing.get(e.getWhoClicked().getUniqueId()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent e)
    {
        if (viewing.containsKey(e.getPlayer().getUniqueId()) && e.getInventory() == viewing.get(e.getPlayer().getUniqueId())) {
            viewing.remove(e.getPlayer().getUniqueId());
        }
    }
}
