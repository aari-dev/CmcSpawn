package dev.aari.cmcspawn.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import dev.aari.cmcspawn.config.ConfigManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldGuardRegionManager {

    private final ConfigManager configManager;
    private final ConcurrentHashMap<UUID, Boolean> regionCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> cacheTime = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5000L;

    public WorldGuardRegionManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean isInAllowedRegion(Player player) {
        List<String> allowedRegions = configManager.getAllowedRegions();

        if (allowedRegions.isEmpty()) {
            return true;
        }

        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        Long lastCheck = cacheTime.get(playerId);
        if (lastCheck != null && (currentTime - lastCheck) < CACHE_DURATION) {
            Boolean cached = regionCache.get(playerId);
            if (cached != null) {
                return cached;
            }
        }

        boolean result = checkRegions(player, allowedRegions);
        regionCache.put(playerId, result);
        cacheTime.put(playerId, currentTime);

        return result;
    }

    private boolean checkRegions(Player player, List<String> allowedRegions) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));

            if (regionManager == null) {
                return false;
            }

            ApplicableRegionSet regions = regionManager.getApplicableRegions(
                    BukkitAdapter.asBlockVector(player.getLocation())
            );

            return regions.getRegions().stream()
                    .anyMatch(region -> allowedRegions.contains(region.getId()));

        } catch (Exception e) {
            return false;
        }
    }

    public void clearCache(UUID playerId) {
        regionCache.remove(playerId);
        cacheTime.remove(playerId);
    }

    public void clearAllCache() {
        regionCache.clear();
        cacheTime.clear();
    }
}
