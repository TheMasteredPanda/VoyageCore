package cm.pvp.voyagepvp.voyagecore.features.itemstacker;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.locale.Format;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import cm.pvp.voyagepvp.voyagecore.api.reflect.ReflectUtil;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.ConstructorAccessor;
import cm.pvp.voyagepvp.voyagecore.api.reflect.accessor.MethodAccessor;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

/*
    Permission Methods:
    voyagecore.itemstacker.<material>.<size>
 */
public class ItemStacker extends Feature implements Listener
{
    private MethodAccessor AS_NMS_COPY;
    private MethodAccessor GET_NBT_TAG;
    private MethodAccessor SET_NBT_TAG;
    private MethodAccessor SET_INT;
    private MethodAccessor<Integer> GET_INT;
    private MethodAccessor<ItemStack> AS_BUKKIT_COPY;
    private ConstructorAccessor NBTCOMPOUND_CONSTRUCTOR;

    @Getter
    private HashMap<Material, Integer> customDefaultStackSizes = Maps.newHashMap();
    private MethodAccessor SET_COUNT;

    public ItemStacker(VoyageCore instance)
    {
        super(instance, "ItemStacker", 1.0);
        Class craftItemStack = ReflectUtil.getOBCClass("inventory.CraftItemStack");
        AS_NMS_COPY = ReflectUtil.getMethod(craftItemStack, "asNMSCopy", true, ItemStack.class);
        Class nmsItemStack = ReflectUtil.getNMSClass("ItemStack");
        GET_NBT_TAG = ReflectUtil.getMethod(nmsItemStack, "getTag", true);
        Class nbtCompound = ReflectUtil.getNMSClass("NBTTagCompound");
        NBTCOMPOUND_CONSTRUCTOR = ReflectUtil.getConstructor(nbtCompound, true);
        SET_NBT_TAG = ReflectUtil.getMethod(nmsItemStack, "setTag", true, nbtCompound);
        SET_INT = ReflectUtil.getMethod(nbtCompound, "setInt", true, String.class, int.class);
        GET_INT = ReflectUtil.getMethod(nbtCompound, "getInt", true, String.class);
        AS_BUKKIT_COPY = ReflectUtil.getMethod(craftItemStack, "asBukkitCopy", true, nmsItemStack);
        SET_COUNT = ReflectUtil.getMethod(nmsItemStack, "setCount", true, int.class);

        for (String key : getSection().getConfigurationSection("default").getKeys(true)) {
            if (Material.getMaterial(key.toUpperCase()) == null) {
                getLogger().warning("Couldn't find the material " + key.toUpperCase() + ".");
                continue;
            }

            if (!NumberUtil.parseable(getSection().getString("default." + key), int.class)) {
                getLogger().warning("Couldn't parse value attached to key " + key.toUpperCase() + ".");
                continue;
            }

            customDefaultStackSizes.put(Material.getMaterial(key.toUpperCase()), getSection().getInt("default." + key));
        }
    }

    @Override
    protected boolean enable() throws Exception
    {
        getInstance().register(new StackCommand(this));
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    public int getAmount(ItemStack item)
    {
        Object nmsItem = AS_NMS_COPY.invoke(null, item);

        if (GET_NBT_TAG.invoke(nmsItem) == null) {
            SET_NBT_TAG.invoke(nmsItem, NBTCOMPOUND_CONSTRUCTOR.invoke());
        }

        return GET_INT.invoke(GET_NBT_TAG.invoke(nmsItem), "voyageCoreItemStacker");
    }

    public ItemStack setAmount(ItemStack entry, int amount, boolean newNbt)
    {
        Object nmsItem = AS_NMS_COPY.invoke(null, entry);

        if (GET_NBT_TAG.invoke(nmsItem) == null || newNbt) {
            SET_NBT_TAG.invoke(nmsItem, NBTCOMPOUND_CONSTRUCTOR.invoke());
        }

        SET_INT.invoke(GET_NBT_TAG.invoke(nmsItem), "voyageCoreItemStacker", amount);

        ItemStack stack = AS_BUKKIT_COPY.invoke(null, nmsItem);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = getSection().getStringList("lore");
        lore.replaceAll(s -> Format.colour(Format.format(s, "{amount};" + String.valueOf(amount))));
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemStack removeNBTTag(ItemStack entry)
    {
        Object nmsItem = AS_NMS_COPY.invoke(null, entry);

        if (GET_NBT_TAG.invoke(nmsItem) != null) {
            SET_NBT_TAG.invoke(nmsItem, NBTCOMPOUND_CONSTRUCTOR.invoke());
        }

        ItemStack stack = AS_BUKKIT_COPY.invoke(null, nmsItem);
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(null);
        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void on(BlockPlaceEvent e)
    {
        int size = getAmount(e.getItemInHand());

        if (size == 0) {
            return;
        }

        size--;

        e.getPlayer().setItemInHand(setAmount(e.getItemInHand(), size, false));

        if (size > e.getItemInHand().getMaxStackSize()) {
            e.getItemInHand().setAmount(e.getItemInHand().getMaxStackSize());
        } else {
            e.getPlayer().setItemInHand(removeNBTTag(e.getItemInHand()));
        }
    }

    @EventHandler
    public void on(PlayerItemConsumeEvent e)
    {
        int size = getAmount(e.getItem());

        if (size == 0) {
            return;
        }

        size--;

        if (size > e.getItem().getType().getMaxStackSize()) {
            Object nmsItem = AS_NMS_COPY.invoke(null, e.getItem());
            SET_COUNT.invoke(nmsItem, e.getItem().getType().getMaxStackSize());
            e.setItem(AS_BUKKIT_COPY.invoke(null, nmsItem));
            e.setItem(setAmount(e.getItem(), size, false));
            e.getPlayer().updateInventory();
        } else {
            e.setItem(removeNBTTag(e.getItem()));
            e.getPlayer().updateInventory();
        }
    }
}
