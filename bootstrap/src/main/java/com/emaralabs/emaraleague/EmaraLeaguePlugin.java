package com.emaralabs.emaraleague;

import com.emaralabs.emaraleague.command.EmaraLeagueCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EmaraLeaguePlugin extends JavaPlugin {

    private static EmaraLeaguePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        
        EmaraLeagueCommand command = new EmaraLeagueCommand(this);
        getCommand("emaraleague").setExecutor(command);
        getCommand("emaraleague").setTabCompleter(command);
        
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
