package com.emaralabs.emaraleague;

import com.emaralabs.emaraleague.command.EmaraLeagueCommand;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EmaraLeaguePlugin extends JavaPlugin {

    private static EmaraLeaguePlugin instance;
    private TournamentManager tournamentManager;

    @Override
    public void onEnable() {
        instance = this;
        tournamentManager = new TournamentManager();

        EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager);
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

    public TournamentManager getTournamentManager() {
        return tournamentManager;
    }
}
