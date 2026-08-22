package com.emaralabs.emaraleague.core.config;

import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private final Plugin plugin;
    private String language = "en";
    private boolean debug = false;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
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

    public void reload() {
    }
}
