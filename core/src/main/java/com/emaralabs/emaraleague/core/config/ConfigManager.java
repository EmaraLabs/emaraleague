package com.emaralabs.emaraleague.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Manages plugin configuration with versioning and migration.
 * Automatically updates config.yml to latest version while preserving user data.
 */
public class ConfigManager {

    private static final int CURRENT_CONFIG_VERSION = 2;

    private final Plugin plugin;
    private FileConfiguration config;
    private File configFile;

    private String language = "en";
    private boolean debug = false;
    private int countdownSeconds = 10;
    private double fallThreshold = 0;
    private boolean autoAssignArena = true;
    private String defaultMode = "duels";
    private int maxConcurrentMatches = 4;
    private boolean bossbarCountdown = true;
    private boolean scoreboardEnabled = true;
    private int matchTimeoutSeconds = 300; // 5 minutes
    private int disconnectGraceSeconds = 60;
    private boolean logoutGuardEnabled = true;
    private String logoutMessage = "<red>%player% has logged out and is disqualified from the tournament!";
    private boolean countdownTitles = true;
    private boolean victoryFireworks = true;
    private boolean killAnnouncements = true;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Check and migrate config version
        int version = config.getInt("config-version", 1);
        if (version < CURRENT_CONFIG_VERSION) {
            migrateConfig(version);
        }

        loadValues();
    }

    private void saveDefaultConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    configFile.createNewFile();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save config.yml", e);
        }
    }

    /**
     * Migrate config to latest version while preserving user data.
     * Adds new keys with defaults, keeps existing user values.
     * P0-003 FIX: Verify all existing values are preserved, only add missing keys.
     */
    private void migrateConfig(int oldVersion) {
        plugin.getLogger().info("Migrating config.yml from version " + oldVersion + " to " + CURRENT_CONFIG_VERSION + "...");

        // Load default config from JAR
        YamlConfiguration defaults = new YamlConfiguration();
        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            if (in != null) {
                defaults.load(new InputStreamReader(in));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load default config for migration: " + e.getMessage());
            return;
        }

        // Track added and preserved keys for verification
        int addedKeys = 0;
        int preservedKeys = 0;

        // Merge: add missing keys from defaults, keep existing user values
        for (String key : defaults.getKeys(true)) {
            if (!config.contains(key)) {
                Object defaultValue = defaults.get(key);
                config.set(key, defaultValue);
                plugin.getLogger().info("  + Added new config key: " + key + " = " + defaultValue);
                addedKeys++;
            } else {
                // Key exists — verify user value is preserved
                Object userValue = config.get(key);
                plugin.getLogger().fine("  ✓ Preserved existing config: " + key + " = " + userValue);
                preservedKeys++;
            }
        }

        // Set new config version
        config.set("config-version", CURRENT_CONFIG_VERSION);

        // Save migrated config
        try {
            config.save(configFile);
            plugin.getLogger().info("Config migration complete. Version " + CURRENT_CONFIG_VERSION +
                    " (added: " + addedKeys + ", preserved: " + preservedKeys + ")");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save migrated config.yml", e);
        }
    }

    private void loadValues() {
        language = config.getString("language", "en");
        debug = config.getBoolean("debug", false);
        countdownSeconds = config.getInt("countdown-seconds", 10);
        fallThreshold = config.getDouble("arena.fall-threshold", 0);
        autoAssignArena = config.getBoolean("arena.auto-assign", true);
        defaultMode = config.getString("match.default-mode", "duels");
        maxConcurrentMatches = config.getInt("match.max-concurrent", 4);
        bossbarCountdown = config.getBoolean("ui.bossbar-countdown", true);
        scoreboardEnabled = config.getBoolean("ui.scoreboard", true);
        matchTimeoutSeconds = config.getInt("match.timeout-seconds", 300);
        disconnectGraceSeconds = config.getInt("match.disconnect-grace-seconds", 60);
        logoutGuardEnabled = config.getBoolean("match.logout-guard", true);
        logoutMessage = config.getString("match.logout-message", "<red>%player% has logged out and is disqualified from the tournament!");
        countdownTitles = config.getBoolean("ui.countdown-titles", true);
        victoryFireworks = config.getBoolean("ui.victory-fireworks", true);
        killAnnouncements = config.getBoolean("ui.kill-announcements", true);
    }

    public void reload() {
        loadConfig();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public double getFallThreshold() {
        return fallThreshold;
    }

    public boolean isAutoAssignArena() {
        return autoAssignArena;
    }

    public String getDefaultMode() {
        return defaultMode;
    }

    public int getMaxConcurrentMatches() {
        return maxConcurrentMatches;
    }

    public boolean isBossbarCountdown() {
        return bossbarCountdown;
    }

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public int getMatchTimeoutSeconds() {
        return matchTimeoutSeconds;
    }

    public int getDisconnectGraceSeconds() {
        return disconnectGraceSeconds;
    }

    public boolean isLogoutGuardEnabled() {
        return logoutGuardEnabled;
    }

    public String getLogoutMessage() {
        return logoutMessage;
    }

    public boolean isCountdownTitles() {
        return countdownTitles;
    }

    public boolean isVictoryFireworks() {
        return victoryFireworks;
    }

    public boolean isKillAnnouncements() {
        return killAnnouncements;
    }
}
