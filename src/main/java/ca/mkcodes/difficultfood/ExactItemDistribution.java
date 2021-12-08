package ca.mkcodes.difficultfood;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExactItemDistribution implements ItemDistribution {
    private final ItemStack item;

    public ExactItemDistribution(@NotNull Material type, int amount) {
        this.item = new ItemStack(type, amount);
    }

    @Override
    public ItemStack sample() {
        return this.item;
    }
}
