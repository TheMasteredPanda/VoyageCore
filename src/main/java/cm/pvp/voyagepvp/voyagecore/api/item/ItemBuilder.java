package cm.pvp.voyagepvp.voyagecore.api.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Map;

/**
 * A builder utility to build items on the fly.
 */
public class ItemBuilder
{
    private ItemStack item;

    private ItemBuilder(ItemStack item)
    {
        this.item = item;
    }

    /**
     * Instantiate a new builder.
     * @param material - material the item will be.
     * @return an Item Builder instance.
     */
    public static ItemBuilder type(Material material)
    {
        return new ItemBuilder(new ItemStack(material));
    }

    /**
     * Set the material data for the item.
     * @param data - material data.
     * @return Item Builder instance.
     */
    public ItemBuilder data(MaterialData data)
    {
        item.setData(data);
        return this;
    }

    /**
     * Set the amount this item instance will represent.
     * @param amount - the amount.
     * @return Item Builder instance.
     */
    public ItemBuilder amount(int amount)
    {
        if (amount == 0) {
            throw new NumberFormatException("Item amount cannot be 0.");
        }

        item.setAmount(amount);
        return this;
    }

    /**
     * Set the durability of the item.
     * @param durability - durability value.
     * @return Item Builder instance.
     */
    public ItemBuilder durability(short durability)
    {
        if (durability == 0) {
            throw new NumberFormatException("Item durability cannot be 0.");
        }

        item.setDurability(durability);
        return this;
    }

    /**
     * Set the meta of the item.
     * @param meta - item meta instance to manipulate.
     * @return Item Builder instance.
     */
    public ItemBuilder meta(ItemMeta meta)
    {
        if (meta == null) {
            throw new NumberFormatException("Item meta cannot be null.");
        }

        item.setItemMeta(meta);
        return this;
    }

    /**
     * Add a map of enchantments on the item.
     * @param enchants - map of enchantments to add.
     * @return Item Builder instance.
     */
    public ItemBuilder enchants(Map<Enchantment, Integer> enchants)
    {
        item.addUnsafeEnchantments(enchants);
        return this;
    }

    /**
     * Enchant the item with one enchantment.
     * @param enchantment - enchantment instance.
     * @param integer - the level the enchantment will be donned at.
     * @return Item Builder instance.
     */
    public ItemBuilder enchant(Enchantment enchantment, Integer integer)
    {
        item.addUnsafeEnchantment(enchantment, integer);
        return this;
    }

    /**
     * Returns the built item.
     * @return ItemStack instance.
     */
    public ItemStack item()
    {
        return item;
    }
}
