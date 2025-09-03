package dev.aari.cmcspawn.config;

import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;

    private double boostHeight;
    private double forwardMotion;
    private long jumpWindow;
    private long cooldownTime;
    private Sound jumpSound;
    private List<String> allowedRegions;
    private String permission;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfigValues();
    }

    public void loadConfigValues() {
        plugin.reloadConfig();

        boostHeight = plugin.getConfig().getDouble("boost.height", 1.5);
        forwardMotion = plugin.getConfig().getDouble("boost.forward", 2.5);
        jumpWindow = plugin.getConfig().getLong("jump.window-ms", 500);
        cooldownTime = plugin.getConfig().getLong("jump.cooldown-ms", 1000);
        permission = plugin.getConfig().getString("permission", "cmcspawn.jump");

        String soundName = plugin.getConfig().getString("sound", "ENTITY_ENDER_DRAGON_FLAP");
        try {
            jumpSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            jumpSound = Sound.ENTITY_ENDER_DRAGON_FLAP;
            plugin.getLogger().warning("Invalid sound: " + soundName + ", using default.");
        }

        allowedRegions = plugin.getConfig().getStringList("regions");
        if (allowedRegions.isEmpty()) {
            String singleRegion = plugin.getConfig().getString("region", "");
            if (!singleRegion.isEmpty()) {
                allowedRegions = Collections.singletonList(singleRegion);
            }
        }
        allowedRegions.removeIf(String::isEmpty);
    }

    public double getBoostHeight() {
        return boostHeight;
    }

    public double getForwardMotion() {
        return forwardMotion;
    }

    public long getJumpWindow() {
        return jumpWindow;
    }

    public long getCooldownTime() {
        return cooldownTime;
    }

    public Sound getJumpSound() {
        return jumpSound;
    }

    public List<String> getAllowedRegions() {
        return allowedRegions;
    }

    public String getPermission() {
        return permission;
    }
}
