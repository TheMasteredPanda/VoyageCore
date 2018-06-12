package cm.pvp.voyagepvp.voyagecore.api.item;

import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Utility builder to manipulate ItemMeta instances.
 */
public class MetaBuilder
{
    private static ItemMeta meta;

    private MetaBuilder(ItemMeta meta)
    {
        this.meta = meta;
    }

    /**
     * Instantiate a Meta Builder instance.
     * @param meta - ItemMeta instance to manipulate.
     * @return Meta Builder instance.
     */
    public static MetaBuilder use(ItemMeta meta)
    {
        return new MetaBuilder(meta);
    }

    /**
     * Set the name of the item.
     * @param name - name of the item.
     * @return Meta Builder instance.
     */
    public MetaBuilder name(String name)
    {
        meta.setDisplayName(name);
        return this;
    }

    /**
     * Set the lore of the item.
     * @param lore - lore.
     * @return Meta Builder instance.
     */
    public MetaBuilder lore(List<String> lore)
    {
        meta.setLore(lore);
        return this;
    }

    /**
     * Set the item flags of the item. If an item flag is already present on an item,
     * it will not be added.
     * @param flags - item flags.
     * @return Meta Builder instance.
     */
    public MetaBuilder flags(ItemFlag... flags)
    {
        List<ItemFlag> sortedFlags = Lists.newArrayList();

        for (ItemFlag flag : flags) {
            if (!meta.hasItemFlag(flag)) {
               sortedFlags.add(flag);
            }
        }

        meta.addItemFlags(sortedFlags.toArray(new ItemFlag[0]));
        return this;
    }

    /**
     * Set the item to be unbreakable or not.
     * @return Meta Builder instance.
     */
    public MetaBuilder unbeakable()
    {
        meta.spigot().setUnbreakable(!meta.spigot().isUnbreakable());
        return this;
    }

    /**
     * Returns the manipulated meta.
     * @return Meta Builder instance.
     */
    public ItemMeta meta()
    {
        return meta;
    }
}
