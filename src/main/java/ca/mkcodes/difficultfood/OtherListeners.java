package ca.mkcodes.difficultfood;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherListeners implements Listener {

    private final Plugin plugin;

    private final HashMap<Material, BoundedNormalDistribution> replaceMap;

    public OtherListeners(Plugin p){
        this.plugin = p;
        this.replaceMap = new HashMap<>();
        p.getServer().getPluginManager().registerEvents(this, p);
        loadFishingConfig(Material.COD);
        loadFishingConfig(Material.TROPICAL_FISH);
        loadFishingConfig(Material.SALMON);
        loadFishingConfig(Material.PUFFERFISH);
    }

    public void loadFishingConfig(Material fishType){
        double lost_chance =plugin.getConfig().getDouble("fish." + fishType.name().toLowerCase());
        if(lost_chance > 0 && lost_chance <= 1){
            BoundedNormalDistribution dist = new BoundedNormalDistribution(lost_chance, 0, 1);
            replaceMap.put(fishType, dist);
            plugin.getLogger().info("Successfully registered fish type: " + fishType.name().toLowerCase());
        }else{
            plugin.getLogger().info("invalid lost_chance for fish: " + fishType.name());
        }

    }

    @EventHandler
    public void onFish(PlayerFishEvent e){
        if(e.getCaught() instanceof Item){
           Item item = (Item) e.getCaught();
           if(this.replaceMap.containsKey(item.getItemStack().getType())){
               if(this.replaceMap.get(item.getItemStack().getType()).sample() == 1){
                   //replace result
                   Location l = item.getLocation();
                   item.remove();
                   Item dropItem = l.getWorld().dropItem(l, new ItemStack(Material.ROTTEN_FLESH, 1));
                   dropItem.setVelocity(e.getPlayer().getLocation().toVector().subtract(l.toVector()).normalize().multiply(new Vector(0.5, 1.55, 0.5)));

               }
           }
        }
    }


}
