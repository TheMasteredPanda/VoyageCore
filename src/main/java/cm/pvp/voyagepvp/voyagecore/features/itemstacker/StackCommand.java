package cm.pvp.voyagepvp.voyagecore.features.itemstacker;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class StackCommand extends VoyageCommand
{
    private ItemStacker feature;
    private Logger logger;

    public StackCommand(ItemStacker feature)
    {
        super(null, "voyagecore.itemstacker.stack", "Stack all items of the same material into one slot.", true, "stack");
        this.feature = feature;
        logger = feature.getLogger();
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        logger.info("Command invoked.");
        Player p = (Player) sender;
        PlayerInventory inv = p.getInventory();

        ArrayListMultimap<MaterialData, ItemStack> stacks = ArrayListMultimap.create();

        IntStream.range(0, inv.getSize()).forEach(i -> {
            ItemStack entry = inv.getItem(i);
            if (entry == null || entry.getType() == Material.AIR) return;
            logger.info("Storing item type " + entry.getType().name() + "/" + String.valueOf(entry.getDurability()) + " previously located at slot" + String.valueOf(i) + " in stacks map. Making item at " + String.valueOf(i) + " null.");
            stacks.put(new MaterialData(entry.getType(), (byte) entry.getDurability()), entry);
            inv.setItem(i, null);
        });

        logger.info("Storing stacks. Key Count: " + String.valueOf(stacks.keySet().size()));

        for (Iterator<MaterialData> iterator = stacks.keySet().iterator(); iterator.hasNext(); ) {
            MaterialData data = iterator.next();
            Material key = data.getItemType();
            logger.info("Iterating through material key: " + key.name() + ".");
            List<ItemStack> list = stacks.get(data);
            int size = 0;
            ItemStack clone = new ItemStack(key, 1, data.getData());
            if (key == Material.AIR) continue;

            if (!feature.getCustomDefaultStackSizes().containsKey(key)) {
                logger.info("This key hasn't got a custom default stack size. Iterating past " + key.name() + ".");
                continue;
            }

            for (ItemStack stack : list) {
                int stackSize = feature.getAmount(stack);
                if (stackSize == 0) stackSize = stack.getAmount();
                size = size + stackSize;
            }

            logger.info("Total stack size of similar items with the material type " + key.name() + " is: " + String.valueOf(size));

            List<ItemStack> items = Lists.newArrayList();
            int maxSize = feature.getCustomDefaultStackSizes().get(key);

            logger.info("Maximum stack size for stacks is " + String.valueOf(size) + ".");

            while (size > 0) {
                logger.info("Size is not 0.");
                ItemStack stack = clone.clone();

                if (size >= maxSize) {
                    logger.info("Size is bigger than ,or equal to, max stack size.");
                    if (maxSize >= stack.getType().getMaxStackSize()) {
                        logger.info("Size is bigger or equal to " + String.valueOf(stack.getType().getMaxStackSize()) + ". Setting visual stack amount to 64 and custom stack amount to " + String.valueOf(maxSize) + ".");
                        stack.setAmount(stack.getType().getMaxStackSize());
                        stack = feature.setAmount(stack, maxSize, false);
                    } else {
                        logger.info("Size is smaller than 64, Setting visual stack amount, and normal stack amount to " + String.valueOf(maxSize));
                        stack.setAmount(maxSize);
                        stack = feature.removeNBTTag(stack);
                    }

                    size = size - maxSize;
                } else {
                    logger.info("Size is smaller than maximum stack size.");

                    if (maxSize >= 64) {
                        logger.info("Size is bigger than or equal to 64, Setting visual stack amount to 64 and custom stack amount to " + String.valueOf(size));
                        stack.setAmount(stack.getType().getMaxStackSize());
                        stack = feature.setAmount(stack, size, false);
                    } else {
                        logger.info("Size is small than 64, Setting visual stack amount and normal stack amount to " + String.valueOf(size));
                        stack.setAmount(size);
                        stack = feature.removeNBTTag(stack);
                    }

                    size = size - maxSize;
                }

                items.add(stack);
                logger.info("Adding stack with material type: " + stack.getType().name() + ".");

                if (items.size() > 54) {
                    logger.info("Sorted items size is bigger than 54.");
                }

                if (size <= 0) {
                    logger.info("Amount for " + key.name() + " is equal to or smaller than 0, passing onto the next material key.");
                    iterator.remove();
                }
            }

            logger.info("Adding all sorted items to the players inventory. Sorted Item List Size: " + items.size());
            inv.addItem(items.toArray(new ItemStack[0]));
        }

        logger.info("Adding sll unsorted items to the players inventory.");
        inv.addItem(stacks.values().toArray(new ItemStack[0]));
    }
}