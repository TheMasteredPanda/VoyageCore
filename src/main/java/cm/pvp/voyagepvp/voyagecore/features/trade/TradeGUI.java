package cm.pvp.voyagepvp.voyagecore.features.trade;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.of;
import static org.bukkit.event.inventory.InventoryAction.*;

public class TradeGUI implements Listener
{
    private Trade feature;
    private Inventory gui;
    private ImmutableList<Integer> tradeArea1 = of(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30);
    private ImmutableList<Integer> tradeArea2 = of(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35);
    private ImmutableList<Integer> trader1AgreeButton = of(45, 46, 47);
    private ImmutableList<Integer> trader2AgreeButton = of(51, 52, 53);
    private ItemStack agreeButton;
    private ItemStack disagreeButton;
    private Player trader1;
    private Player trader2;
    private boolean closing = false;
    private boolean dealStruck = false;
    private List<UUID> agree = Lists.newArrayList();

    public TradeGUI(Trade feature, Player trader1, Player trader2)
    {

        this.feature = feature;
        this.trader1 = trader1;
        this.trader2 = trader2;
        gui = Bukkit.createInventory(null, 54, "Trade GUI");
        ItemStack divider = new ItemStack(Material.STAINED_GLASS_PANE);
        gui.setItem(4, divider);
        gui.setItem(13, divider);
        gui.setItem(22, divider);
        gui.setItem(31, divider);
        IntStream.range(36, 45).forEach(i -> gui.setItem(i, divider));
        agreeButton = new ItemStack(Material.WOOL, 1, (byte) 5);
        disagreeButton = new ItemStack(Material.WOOL, 1, (byte) 14);
        trader1AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
        trader2AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
        trader1.openInventory(gui);
        trader2.openInventory(gui);
        Bukkit.getPluginManager().registerEvents(this, feature.getInstance());
    }

    @EventHandler
    public void on(InventoryCloseEvent ev)
    {
        if (ev.getViewers().stream().allMatch(e -> e.getUniqueId().equals(trader1.getUniqueId()) || e.getUniqueId().equals(trader2.getUniqueId())) && !closing) {
            closing = true;

            trader1.closeInventory();
            trader2.closeInventory();

            if (!dealStruck) {
                returnItems(trader1, tradeArea1);
                returnItems(trader2, tradeArea2);
            }

            HandlerList.unregisterAll(this);
        }
    }

    public void returnItems(Player trader, List<Integer> area)
    {
        for (int slot : area) {
            ItemStack entry = gui.getItem(slot);

            if (entry == null || entry.getType() == Material.AIR) {
                continue;
            }

            giveItem(entry, trader);
        }
    }

    public void giveItem(ItemStack item, Player trader)
    {
        if (trader.getInventory().firstEmpty() == -1) {
            trader.getWorld().dropItem(trader.getLocation(), item);
        } else {
            trader.getInventory().addItem(item);
        }
    }

    @EventHandler
    public void on(InventoryClickEvent e)
    {
        if (!e.getInventory().getTitle().equals("Trade GUI")) return;
        if (e.getViewers().stream().noneMatch(human -> human.getUniqueId().equals(trader2.getUniqueId()) || human.getUniqueId().equals(trader1.getUniqueId()))) return;
        if (e.getRawSlot() == -999) return;
        if (IntStream.range(36, 45).anyMatch(slot -> e.getRawSlot() == slot) || of(4, 13, 22, 31).contains(e.getRawSlot())) {
            e.setCancelled(true);
            return;
        }

        feature.getLogger().info("Clicked Slot: Action: " + e.getAction().name() + " Non-Raw: " + String.valueOf(e.getSlot()) + " / Raw: " + String.valueOf(e.getRawSlot()) + "/ Trader: " + (trader1.getUniqueId().equals(e.getWhoClicked().getUniqueId()) ? trader1.getName() : trader2.getName()));

        Player clicker = (Player) e.getWhoClicked();

        if (trader1AgreeButton.contains(e.getRawSlot())) {
            e.setCancelled(true);

            if (trader2.getUniqueId().equals(clicker.getUniqueId())) {
                return;
            }

            if (trader1.getUniqueId().equals(clicker.getUniqueId())) {
                if (!agree.contains(clicker.getUniqueId())) {
                    trader1AgreeButton.forEach(slot -> gui.setItem(slot, agreeButton));
                    agree.add(clicker.getUniqueId());
                    check();
                }
            }

            return;
        }

        if (trader2AgreeButton.contains(e.getRawSlot())) {
            e.setCancelled(true);

            if (trader1.getUniqueId().equals(clicker.getUniqueId())) {
                return;
            }

            if (trader2.getUniqueId().equals(clicker.getUniqueId())) {
                if (!agree.contains(clicker.getUniqueId())) {
                    trader2AgreeButton.forEach(slot -> gui.setItem(slot, agreeButton));
                    agree.add(clicker.getUniqueId());
                    check();
                }
            }

            return;
        }

        if (e.getAction() == PLACE_SOME || e.getAction() == PLACE_ALL || e.getAction() == PLACE_SOME) {
            trader2AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            trader1AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            agree.clear();

            if (tradeArea1.contains(e.getRawSlot()) && trader2.getUniqueId().equals(clicker.getUniqueId())) {
                feature.getLogger().info(trader2.getName() + " attempted to put something in tradeArea1.");
                place(e, tradeArea2);
                e.setCancelled(true);
            }

            if (tradeArea2.contains(e.getRawSlot()) && trader1.getUniqueId().equals(clicker.getUniqueId())) {
                feature.getLogger().info(trader1.getName() + " attempted to put something in tradeArea2.");
                place(e, tradeArea1);
                e.setCancelled(true);
            }

            return;
        }

        if (of(PICKUP_ALL, PICKUP_ONE, PICKUP_SOME, PICKUP_HALF, SWAP_WITH_CURSOR, HOTBAR_MOVE_AND_READD).contains(e.getAction())) {
            feature.getLogger().info(clicker.getName() + " picked up a stack");
            trader2AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            trader1AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            agree.clear();

            if (tradeArea1.contains(e.getRawSlot()) && trader2.getUniqueId().equals(clicker.getUniqueId())) {
                feature.getLogger().info(trader2.getName() + " attempted to pickup something in tradeArea1.");
                e.setCancelled(true);
            }

            if (tradeArea2.contains(e.getRawSlot()) && trader1.getUniqueId().equals(clicker.getUniqueId())) {
                feature.getLogger().info(trader1.getName() + " attempted to pickup something in tradeArea2.");
                e.setCancelled(true);
            }
        }

        if (e.getAction() == MOVE_TO_OTHER_INVENTORY) {
            if (e.getClickedInventory() != e.getView().getBottomInventory()) return;
            e.setCancelled(true);


            trader2AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            trader1AgreeButton.forEach(slot -> gui.setItem(slot, disagreeButton));
            agree.clear();

            feature.getLogger().info("Current Item Material: " + (e.getCurrentItem() == null ? "Null" : e.getCurrentItem().getType().name()));

            if (clicker.getUniqueId().equals(trader2.getUniqueId())) {
                move(e, e.getCurrentItem(), tradeArea2);
            }

            if (clicker.getUniqueId().equals(trader1.getUniqueId())) {
                move(e, e.getCurrentItem(), tradeArea1);
            }
        }
    }

    public void check()
    {
        if (agree.size() == 2) {
            returnItems(trader2, tradeArea1);
            returnItems(trader1, tradeArea2);
            dealStruck = true;
            trader1.closeInventory();
            return;
        }
    }

    public void move(InventoryClickEvent e, ItemStack item, List<Integer> area)
    {
        int amount = item.getAmount();
        int nullSlot = -1;

        for (int slot : area) {
            ItemStack entry = e.getView().getTopInventory().getItem(slot);

            if (entry == null) {

                if (nullSlot == -1) {
                    nullSlot = slot;
                }

                continue;
            }

            if (entry.isSimilar(item)) {
                if ((entry.getAmount() + amount) > item.getType().getMaxStackSize()) {
                    int diff = (int) Math.floor(item.getType().getMaxStackSize() - entry.getAmount());
                    amount -= diff;
                    entry.setAmount(entry.getAmount() + diff);
                } else {
                    entry.setAmount(entry.getAmount() + amount);
                    amount = 0;
                    break;
                }
            }
        }

        item.setAmount(amount);

        if (nullSlot != -1) {

            if (amount != 0) {
                amount = 0;
                e.getView().getTopInventory().setItem(nullSlot, item);
            }
        }

        if (amount == 0) {
            e.getView().setCursor(null);
            e.setCurrentItem(null);
        }
    }

    public void place(InventoryClickEvent e, List<Integer> area)
    {
        int nullSlot = -1;

        ItemStack item = e.getCursor();
        int amount = item.getAmount();

        for (int slot : area) {
            ItemStack entry = e.getView().getTopInventory().getItem(slot);

            if (entry == null || entry.getType() == Material.AIR) {
                if (nullSlot == -1) {
                    nullSlot = slot;
                }

                continue;
            }

            if (entry.isSimilar(item)) {
                if ((entry.getAmount() + amount) > item.getType().getMaxStackSize()) {
                    int diff = (int) Math.floor(item.getType().getMaxStackSize() - entry.getAmount());
                    entry.setAmount(entry.getAmount() + diff);
                    amount -= diff;
                } else {
                    entry.setAmount(entry.getAmount() + item.getAmount());
                    amount = 0;
                    break;
                }
            }
        }

        if (amount != 0 && nullSlot != -1) {
            e.getView().getTopInventory().setItem(nullSlot, item);
            amount = 0;
        }

        if (amount == 0) {
            e.getView().setCursor(null);
        }
    }

    @EventHandler
    public void on(InventoryDragEvent e)
    {

        if (tradeArea2.stream().anyMatch(slot -> e.getRawSlots().contains(slot)) && trader1.getUniqueId().equals(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }

        if (tradeArea1.stream().anyMatch(slot -> e.getRawSlots().contains(slot)) && trader2.getUniqueId().equals(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
