package com.emaralabs.emaraleague.integrations.placeholderapi;

import org.bukkit.plugin.Plugin;

public class PlaceholderAPIIntegration {

    private final Plugin plugin;
    private boolean available;

    public PlaceholderAPIIntegration(Plugin plugin) {
        this.plugin = plugin;
        this.available = checkAvailability();
    }

    private boolean checkAvailability() {
        if (plugin == null) {
            return false;
        }
        return plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public boolean isAvailable() {
        return available;
    }

    public void registerExpansion() {
        if (available) {
            new EmaraLeagueExpansion().register();
        }
    }
}
