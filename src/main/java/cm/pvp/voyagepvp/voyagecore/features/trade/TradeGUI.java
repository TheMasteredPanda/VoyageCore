package cm.pvp.voyagepvp.voyagecore.features.trade;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class TradeGUI implements Listener
{
    private Inventory gui;
    private ImmutableList<Integer> tradeArea1;
    private ImmutableList<Integer> tradeArea2;
    private Player trader1;
    private Player trader2;

    //Button slot numbers.
    private int increaseMoney = 0;
    private int decreaseMoney = 0;
    private int exit = 0;
    private int agree = 0;

    private List<UUID> closeInventoryIgnore = Lists.newArrayList();
    private List<UUID> agreeList = Lists.newArrayList();

    public TradeGUI(Player trader1, Player trader2)
    {
        this.trader1 = trader1;
        this.trader2 = trader2;
        gui = Bukkit.createInventory(null, 54);
        tradeArea1 = ImmutableList.of(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30);
        tradeArea2 = ImmutableList.of(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35);
        ItemStack divider = new ItemStack(Material.STAINED_GLASS_PANE);
        gui.setItem(4, divider);
        gui.setItem(13, divider);
        gui.setItem(22, divider);
        gui.setItem(31, divider);
        IntStream.rangeClosed(36, 45).forEach(i -> gui.setItem(i, divider));
        trader1.openInventory(gui);
        trader2.openInventory(gui);
    }

    @EventHandler
    public void on(InventoryCloseEvent e)
    {
        if (e.getPlayer().getUniqueId().equals(trader1.getUniqueId())) {
            if (closeInventoryIgnore.contains(trader1.getUniqueId())) {
                closeInventoryIgnore.remove(trader1.getUniqueId());
                return;
            }

            closeInventoryIgnore.add(trader1.getUniqueId());
            trader2.closeInventory();
        }

        if (e.getPlayer().getUniqueId().equals(trader2.getUniqueId())) {
            if (closeInventoryIgnore.contains(trader2.getUniqueId())) {
                closeInventoryIgnore.remove(trader2.getUniqueId());
                return;
            }

            closeInventoryIgnore.add(trader2.getUniqueId());
            trader1.closeInventory();
        }
    }

    @EventHandler
    public void on(InventoryClickEvent e)
    {
        if (!e.getWhoClicked().getUniqueId().equals(trader1.getUniqueId()) || !e.getWhoClicked().getUniqueId().equals(trader2.getUniqueId())) {
            return;
        }

        if (e.getSlot() == -999) {
            return;
        }

        if (e.getSlot() == 4 || e.getSlot() == 13 || e.getSlot() == 22 || e.getSlot() == 31 ||
        e.getSlot() == 36 || e.getSlot() == 37 || e.getSlot() == 38 || e.getSlot() == 39 || e.getSlot() == 40
        || e.getSlot() == 41 || e.getSlot() == 42 || e.getSlot() == 43 || e.getSlot() == 44 || e.getSlot() == 45) {
            e.setCancelled(true);
            return;
        }

        if (trader2.getUniqueId().equals(e.getWhoClicked().getUniqueId())) {
            if (tradeArea1.contains(e.getSlot())) {
                e.setCancelled(true);
                return;
            }
        }

        if (trader1.getUniqueId().equals(e.getWhoClicked().getUniqueId())) {
            if (tradeArea2.contains(e.getSlot())) {
                e.setCancelled(true);
            }
        }
    }
}
