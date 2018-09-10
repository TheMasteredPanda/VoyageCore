package cm.pvp.voyagepvp.voyagecore.features.customdrops;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public class CustomMobEntry
{
    private ItemStack item;
    private int min;
    private int max;
}
