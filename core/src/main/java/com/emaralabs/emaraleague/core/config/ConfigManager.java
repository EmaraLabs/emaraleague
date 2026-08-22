package com.emaralabs.emaraleague.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigManager {

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
}
