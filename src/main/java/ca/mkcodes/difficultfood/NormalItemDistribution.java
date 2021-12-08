package ca.mkcodes.difficultfood;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NormalItemDistribution implements ItemDistribution{

    private BoundedNormalDistribution n;

    private final Material type;


    public NormalItemDistribution(@NotNull Material type, double mean, int min, int max){
        this.type = type;
        this.n = new BoundedNormalDistribution(mean, min, max);
    }

    @Override
    public ItemStack sample(){
        int quantity = this.n.sample();
        if(quantity > 0){
            return new ItemStack(this.type, quantity);
        }
        return null;
    }

    public Material getType() {
        return type;
    }


}
