package cm.pvp.voyagepvp.voyagecore.features.customdrops;

import cm.pvp.voyagepvp.voyagecore.Feature;
import cm.pvp.voyagepvp.voyagecore.VoyageCore;
import cm.pvp.voyagepvp.voyagecore.api.math.NumberUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class CustomDrops extends Feature implements Listener
{
    private Random r = new Random();
    private ArrayListMultimap<EntityType, CustomMobEntry> drops = ArrayListMultimap.create();

    public CustomDrops(VoyageCore instance)
    {
        super(instance, "CustomDrops", 1.0);

        for (String key : getSection().getConfigurationSection("drops").getKeys(false)) {
            if (Stream.of(EntityType.values()).noneMatch(type -> type.name().equalsIgnoreCase(key.toUpperCase()))) {
                getLogger().warning(key.toUpperCase() + " is not an entity type.");
                continue;
            }

            EntityType type = EntityType.valueOf(key.toUpperCase());


            Set<String> keys = getSection().getConfigurationSection("drops." + key).getKeys(false);

            for (String materialKey : keys) {
                short data = -1;
                Material material = null;
                String[] split = materialKey.split(":");

                if (split.length == 1) {
                    material = Material.getMaterial(materialKey.toUpperCase());
                }

                if (split.length == 2) {
                    data = NumberUtil.parse(split[1], byte.class);
                    material = Material.getMaterial(split[0]);
                }

                if (material == null) {
                    getLogger().info(materialKey.toUpperCase() + " is not a material type.");
                    continue;
                }

                ItemStack stack = new ItemStack(material, 1, data == -1 ? 0 : data);

                int min = getSection().getInt("drops." + key + "." + materialKey + ".min");
                int max = getSection().getInt("drops." + key + "." + materialKey + ".max");
                CustomMobEntry entry = new CustomMobEntry(stack, min, max);
                drops.put(type, entry);
            }
        }
    }

    @Override
    protected boolean enable() throws Exception
    {
        Bukkit.getPluginManager().registerEvents(this, getInstance());
        return true;
    }

    @EventHandler
    public void on(EntityDeathEvent e)
    {
        if (!drops.containsKey(e.getEntity().getType())) {
            return;
        }

        List<CustomMobEntry> entries = drops.get(e.getEntity().getType());
        ArrayList<ItemStack> newDrops = Lists.newArrayList();

        for (CustomMobEntry entry : entries) {
            int amount = r.nextInt(entry.getMax()) + entry.getMin();
            ItemStack clone = entry.getItem();
            clone.setAmount(amount);
            newDrops.add(clone);
        }

        e.getDrops().clear();
        e.getDrops().addAll(newDrops);
    }
}
