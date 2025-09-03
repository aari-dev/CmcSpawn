package dev.aari.cmcspawn;

import dev.aari.cmcspawn.config.ConfigManager;
import dev.aari.cmcspawn.listeners.JumpListener;
import dev.aari.cmcspawn.managers.JumpManager;
import dev.aari.cmcspawn.managers.WorldGuardRegionManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CmcSpawn extends JavaPlugin {

    private ConfigManager configManager;
    private JumpManager jumpManager;
    private WorldGuardRegionManager regionManager;
    private JumpListener jumpListener;

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            getLogger().severe("WorldGuard not found! Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.regionManager = new WorldGuardRegionManager(configManager);
        this.jumpManager = new JumpManager(this, configManager);
        this.jumpListener = new JumpListener(this, jumpManager, regionManager, configManager);

        getServer().getPluginManager().registerEvents(jumpListener, this);

        getLogger().info("CmcSpawn plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (jumpManager != null) {
            jumpManager.cleanup();
        }

        for (Player player : getServer().getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL && player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }

        getLogger().info("CmcSpawn plugin disabled successfully!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public JumpManager getJumpManager() {
        return jumpManager;
    }

    public WorldGuardRegionManager getRegionManager() {
        return regionManager;
    }
}
