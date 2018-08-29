package cm.pvp.voyagepvp.voyagecore.features.itemstacker;

import cm.pvp.voyagepvp.voyagecore.api.command.VoyageCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class StackCommand extends VoyageCommand
{
    private ItemStacker feature;

    public StackCommand(ItemStacker feature)
    {
        super(null, "voyagecore.itemstacker.stack", "Stack all items of the same material into one slot.", true, "stack");
        this.feature = feature;
    }

    @Override
    public void execute(CommandSender sender, VoyageCommand command, LinkedList<String> arguments)
    {
        Player p = (Player) sender;
        Inventory inv = p.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack entry = inv.getItem(i);

            if (entry == null || entry.getType() == Material.AIR) {
                continue;
            }

            int first = inv.first(entry.getType());

            if (first == i) {
                continue;
            }

            ItemStack firstStack = inv.getItem(first);

            if (entry.getAmount() + firstStack.getAmount() <= firstStack.getType().getMaxStackSize()) {
                firstStack.setAmount(entry.getAmount() + firstStack.getAmount());
            } else {
                int size = feature.getAmount(firstStack);

                if (size == 0) {
                    size = firstStack.getAmount();
                }

                int size1 = feature.getAmount(entry);

                if (size1 == 0) {
                    size1 = entry.getAmount();
                }

                inv.setItem(first, feature.setAmount(firstStack, size + size1, false));
            }

            inv.setItem(i, null);
        }
    }
}
