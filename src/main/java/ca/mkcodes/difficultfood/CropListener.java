package ca.mkcodes.difficultfood;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CropListener implements Listener {

    private final Plugin p;

    private final Map<Material, List<ItemDistribution>> newDistributions = new HashMap<>();

    private final List<Material> bonemealReducedCrops;

    private BoundedNormalDistribution bonemealDistribution;

    private double beetrootFactor;

    public CropListener(Plugin p){
        this.p = p;
        this.loadCropConfig(Material.WHEAT, Material.WHEAT, Material.WHEAT_SEEDS);
        this.loadCropConfig(Material.POTATOES, Material.POTATO, null);
        this.loadCropConfig(Material.CARROTS, Material.CARROT, null);
        this.loadCropConfig(Material.BEETROOTS, Material.BEETROOT, Material.BEETROOT_SEEDS);
        this.loadCropConfig(Material.MELON, Material.MELON_SLICE, null);
        this.bonemealReducedCrops = new LinkedList<>();
        this.loadBonemealSettings();
    }

    private void loadBonemealSettings(){
        Object main = p.getConfig().get("bonemeal");
        if(main != null){
            MemorySection main_mem = (MemorySection) main;
            List<String> reduce = main_mem.getStringList("reduce");
            for (String s : reduce) {
                Material add = Material.matchMaterial(s);
                if(add != null){
                    this.bonemealReducedCrops.add(add);
                }else{
                    this.p.getLogger().warning("Could not find crop: " + s);
                }
            }
            double reduceFactor = main_mem.getDouble("average");
            if(reduceFactor > 0){
                this.bonemealDistribution = new BoundedNormalDistribution(reduceFactor, 0, 4);
            }else{
                this.bonemealDistribution = new BoundedNormalDistribution(3.5, 2, 5);
            }
            double beetrootFactor = main_mem.getDouble("beetroot_factor");
            this.beetrootFactor = (beetrootFactor > 0) ? beetrootFactor: 0.5;
        }
    }

    public void loadCropConfig(Material crop_type, Material main_output, Material alt_output){
        Object main = p.getConfig().get("crops." + main_output.name().toLowerCase());
        if (main != null){
            ItemDistribution i = getDistribution((MemorySection) main, main_output);
            if(i != null){
                List<ItemDistribution> newDist = new ArrayList<>();
                newDist.add(i);
                if(alt_output != null){
                    Object alt = p.getConfig().get("crops." + alt_output.name().toLowerCase());
                    if(alt != null){
                        ItemDistribution i2 = getDistribution((MemorySection)alt, alt_output);
                        if(i2 != null){
                            newDist.add(i2);
                        }
                    }
                }
                newDistributions.put(crop_type, newDist);
                p.getLogger().info("Successfully registered crop type: " + crop_type.name().toLowerCase());
            }
        }else{
            p.getLogger().info("---- nill ---");
        }
    }

    private ItemDistribution getDistribution(MemorySection m, Material type){
        String min = m.getString("min");
        String mean = m.getString("average");
        String max = m.getString("max");
        if(mean != null){
            if (min != null && max != null && (!min.equals(max))){
                try{
                    return new NormalItemDistribution(type, Double.parseDouble(mean), Integer.parseInt(min), Integer.parseInt(max));
                }catch(NumberFormatException e){
                    p.getLogger().warning("Invalid number in the crop configuration for: " + type.name().toLowerCase());
                }
            }else{
                try{
                    return new ExactItemDistribution(type, Integer.parseInt(mean));
                }catch(NumberFormatException e){
                    p.getLogger().warning("Invalid number in the crop configuration for: " + type.name().toLowerCase());
                }
            }
        }else{
            p.getLogger().warning("Invalid crop configuration for: " + type.name().toLowerCase() + ". Make sure it has the properties 'min','average', and 'max.");
        }
        return null;
    }

    @EventHandler
    public void onPistonExtendEvent(BlockPistonExtendEvent e){
        handlePistonEvent(e.getBlocks());
    }

    @EventHandler
    public void onPistonRetractEvent(BlockPistonRetractEvent e){
        handlePistonEvent(e.getBlocks());
    }

    private void handlePistonEvent(List<Block> blocks){
        for(Block b: blocks){
            if(this.newDistributions.containsKey(b.getType())){
                if(b.getBlockData() instanceof Ageable){
                    handleBreak(b);
                }
            }
        }
    }

    @EventHandler
    public void onCropHarvestBreak(BlockBreakEvent e){
        if(this.newDistributions.containsKey(e.getBlock().getType())){
            if(e.getBlock().getBlockData() instanceof Ageable){
                handleBreak(e.getBlock());
            }
        }
    }

    @EventHandler
    public void onCropHarvest(BlockDestroyEvent e){
        if(this.newDistributions.containsKey(e.getBlock().getType())){
            if(e.getBlock().getBlockData() instanceof Ageable){
                handleBreak(e.getBlock());
            }
        }
    }

    @EventHandler
    public void onWaterFlow(BlockFromToEvent e){
        if(this.newDistributions.containsKey(e.getToBlock().getType())){
            if(e.getToBlock().getBlockData() instanceof Ageable){
                handleBreak(e.getToBlock());
            }
        }
    }


    private boolean handleBreak(Block b){
        Ageable a = (Ageable)b.getBlockData();
        if(a.getAge() != a.getMaximumAge()){
            return false;
        }
        b.getDrops().clear();
        for(ItemDistribution i: this.newDistributions.get(b.getType())){
            ItemStack sample = i.sample();
            if(sample != null){
                b.getWorld().dropItemNaturally(b.getLocation(), sample);
            }
        }
        return true;
    }

    @EventHandler
    public void onBlockFertilize2(BlockFertilizeEvent e){
        if(e.getBlocks().size() == 1 && e.getBlock().getBlockData() instanceof Ageable && this.newDistributions.containsKey(e.getBlock().getType())){
            Block block = e.getBlock();
            e.setCancelled(true);
            Player p = e.getPlayer();

            if(p != null && p.getGameMode() != GameMode.CREATIVE){
                EquipmentSlot hand = EquipmentSlot.OFF_HAND;
                if(p.getInventory().getItemInMainHand().getType() == Material.BONE_MEAL){
                    hand = EquipmentSlot.HAND;
                }
                ItemStack item = p.getInventory().getItem(hand);
                item.setAmount(item.getAmount() - 1 );
                if(item.getAmount() == 0){
                    //If run out of bonemeal, remove from inventory
                    p.getInventory().setItem(hand, null);
                }
            }

            Ageable b = (Ageable)block.getBlockData();
            Ageable a = (Ageable)e.getBlocks().get(0).getBlockData();
            double sample = this.bonemealDistribution.sampleRaw();
            if(block.getType() == Material.BEETROOTS){
                sample *= this.beetrootFactor;
            }
            a.setAge(Math.min(a.getMaximumAge(), b.getAge() +(int) Math.round(sample)));
            block.setBlockData(a);

        }
    }

}
