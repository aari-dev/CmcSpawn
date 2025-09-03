package dev.aari.cmcspawn.managers;

import dev.aari.cmcspawn.config.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, Long> lastJumpTime = new ConcurrentHashMap<>();
    private final Set<UUID> jumpCooldown = ConcurrentHashMap.newKeySet();
    private final Set<UUID> playersWithFlight = ConcurrentHashMap.newKeySet();

    private BukkitTask flightTask;

    public JumpManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        startFlightTask();
    }

    private void startFlightTask() {
        flightTask = new BukkitRunnable() {
            @Override
            public void run() {
                updatePlayerFlightStatus();
            }
        }.runTaskTimer(plugin, 20L, 10L);
    }

    private void updatePlayerFlightStatus() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            if (shouldHaveFlight(player)) {
                if (!playersWithFlight.contains(playerId)) {
                    enableFlight(player);
                    playersWithFlight.add(playerId);
                }
            } else {
                if (playersWithFlight.contains(playerId)) {
                    disableFlight(player);
                    playersWithFlight.remove(playerId);
                }
            }
        }
    }

    private boolean shouldHaveFlight(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL &&
                !player.isFlying() &&
                (player.hasPermission(configManager.getPermission()) || player.isOp());
    }

    private void enableFlight(Player player) {
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
        if (player.isFlying()) {
            player.setFlying(false);
        }
    }

    private void disableFlight(Player player) {
        if (player.getAllowFlight() && player.getGameMode() == GameMode.SURVIVAL) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    public boolean handleJumpAttempt(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (jumpCooldown.contains(playerId)) {
            return false;
        }

        Long lastJump = lastJumpTime.get(playerId);

        if (lastJump != null && (currentTime - lastJump) <= configManager.getJumpWindow()) {
            performDoubleJump(player);
            lastJumpTime.remove(playerId);
            startCooldown(playerId);
            return true;
        } else {
            lastJumpTime.put(playerId, currentTime);
            return false;
        }
    }

    private void performDoubleJump(Player player) {
        if (player.isFlying()) {
            player.setFlying(false);
        }

        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();

        Vector velocity = new Vector(
                direction.getX() * configManager.getForwardMotion(),
                configManager.getBoostHeight(),
                direction.getZ() * configManager.getForwardMotion()
        );

        player.setVelocity(velocity);
        player.playSound(loc, configManager.getJumpSound(), 1.0f, 1.0f);

        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby != player && nearby.getLocation().distanceSquared(loc) <= 400) {
                nearby.playSound(loc, configManager.getJumpSound(), 0.5f, 1.0f);
            }
        }
    }

    private void startCooldown(UUID playerId) {
        jumpCooldown.add(playerId);
        new BukkitRunnable() {
            @Override
            public void run() {
                jumpCooldown.remove(playerId);
            }
        }.runTaskLater(plugin, configManager.getCooldownTime() / 50L);
    }

    public void removePlayer(UUID playerId) {
        lastJumpTime.remove(playerId);
        jumpCooldown.remove(playerId);
        playersWithFlight.remove(playerId);
    }

    public void cleanup() {
        if (flightTask != null) {
            flightTask.cancel();
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (playersWithFlight.contains(player.getUniqueId())) {
                disableFlight(player);
            }
        }

        lastJumpTime.clear();
        jumpCooldown.clear();
        playersWithFlight.clear();
    }
}
