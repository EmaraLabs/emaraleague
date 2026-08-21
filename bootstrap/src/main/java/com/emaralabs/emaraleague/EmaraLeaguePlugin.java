package com.emaralabs.emaraleague;

import org.bukkit.plugin.java.JavaPlugin;

public final class EmaraLeaguePlugin extends JavaPlugin {

    private static EmaraLeaguePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("EmaraLeague enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("EmaraLeague disabled");
        instance = null;
    }

    public static EmaraLeaguePlugin getInstance() {
        return instance;
    }
}
