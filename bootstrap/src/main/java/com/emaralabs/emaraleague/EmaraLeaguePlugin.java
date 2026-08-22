package com.emaralabs.emaraleague;

import com.emaralabs.emaraleague.command.EmaraLeagueCommand;
import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.teleport.TeleportService;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import com.emaralabs.emaraleague.listener.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class EmaraLeaguePlugin extends JavaPlugin {

    private static EmaraLeaguePlugin instance;
    private TournamentManager tournamentManager;
    private ArenaManager arenaManager;
    private PlayerSessionManager playerSessionManager;
    private TeleportService teleportService;
    private MatchEngine matchEngine;

    @Override
    public void onEnable() {
        instance = this;
        tournamentManager = new TournamentManager();
        arenaManager = new ArenaManager();
        playerSessionManager = new PlayerSessionManager();
        teleportService = new TeleportService();
        matchEngine = new MatchEngine(tournamentManager, arenaManager);

        EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager);
        getCommand("emaraleague").setExecutor(command);
        getCommand("emaraleague").setTabCompleter(command);

        PlayerEventListener listener = new PlayerEventListener(matchEngine, playerSessionManager, command.getMessages());
        getServer().getPluginManager().registerEvents(listener, this);

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

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public PlayerSessionManager getPlayerSessionManager() {
        return playerSessionManager;
    }

    public TeleportService getTeleportService() {
        return teleportService;
    }

    public MatchEngine getMatchEngine() {
        return matchEngine;
    }
}
