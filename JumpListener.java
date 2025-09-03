package dev.aari.cmcspawn.listeners;

import dev.aari.cmcspawn.config.ConfigManager;
import dev.aari.cmcspawn.managers.JumpManager;
import dev.aari.cmcspawn.managers.WorldGuardRegionManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JumpListener implements Listener {

    private final JavaPlugin plugin;
    private final JumpManager jumpManager;
    private final WorldGuardRegionManager regionManager;
    private final ConfigManager configManager;

    public JumpListener(JavaPlugin plugin, JumpManager jumpManager,
                        WorldGuardRegionManager regionManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.jumpManager = jumpManager;
        this.regionManager = regionManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (!player.hasPermission(configManager.getPermission()) && !player.isOp()) {
            return;
        }

        if (!regionManager.isInAllowedRegion(player)) {
            return;
        }

        if (player.isFlying()) {
            event.setCancelled(true);
            player.setFlying(false);
            return;
        }

        boolean jumpExecuted = jumpManager.handleJumpAttempt(player);

        event.setCancelled(true);
        player.setFlying(false);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        regionManager.clearCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        jumpManager.removePlayer(event.getPlayer().getUniqueId());
        regionManager.clearCache(event.getPlayer().getUniqueId());
    }
}
