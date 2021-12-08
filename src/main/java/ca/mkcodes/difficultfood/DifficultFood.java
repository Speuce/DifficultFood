package ca.mkcodes.difficultfood;

import org.bukkit.plugin.java.JavaPlugin;

public final class DifficultFood extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getLogger().info("---- plugin starts ---");
        this.getServer().getPluginManager().registerEvents(new CropListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
