package com.emaralabs.emaraleague;

import com.emaralabs.emaraleague.command.EmaraLeagueCommand;
import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.game.GameModeRegistry;
import com.emaralabs.emaraleague.core.match.MatchCountdown;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.scheduler.PaperScheduler;
import com.emaralabs.emaraleague.core.teleport.TeleportService;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import com.emaralabs.emaraleague.infrastructure.database.DatabaseManager;
import com.emaralabs.emaraleague.infrastructure.database.TournamentRepository;
import com.emaralabs.emaraleague.listener.PlayerEventListener;
import com.emaralabs.emaraleague.modules.duels.DuelsGameMode;
import com.emaralabs.emaraleague.modules.spleef.SpleefGameMode;
import org.bukkit.plugin.java.JavaPlugin;

public final class EmaraLeaguePlugin extends JavaPlugin {

    private static EmaraLeaguePlugin instance;
    private TournamentManager tournamentManager;
    private ArenaManager arenaManager;
    private PlayerSessionManager playerSessionManager;
    private TeleportService teleportService;
    private MatchEngine matchEngine;
    private GameModeRegistry gameModeRegistry;
    private DatabaseManager databaseManager;
    private TournamentRepository tournamentRepository;
    private WinConditionEvaluator winConditionEvaluator;
    private MatchCountdown matchCountdown;

    @Override
    public void onEnable() {
        instance = this;

        String dbPath = getDataFolder().getAbsolutePath() + "/emaraleague.db";
        databaseManager = new DatabaseManager("jdbc:sqlite:" + dbPath, "", "");
        databaseManager.initializeSchema();
        tournamentRepository = new TournamentRepository(databaseManager);

        tournamentManager = new TournamentManager();
        tournamentManager.setPersistence(tournamentRepository);
        tournamentManager.loadFromDatabase();

        arenaManager = new ArenaManager();
        playerSessionManager = new PlayerSessionManager();
        teleportService = new TeleportService();
        matchEngine = new MatchEngine(tournamentManager, arenaManager);
        gameModeRegistry = new GameModeRegistry();
        winConditionEvaluator = new WinConditionEvaluator(playerSessionManager);
        matchCountdown = new MatchCountdown(new PaperScheduler(this), null);

        gameModeRegistry.register(new DuelsGameMode());
        gameModeRegistry.register(new SpleefGameMode());
        matchEngine.setGameModeRegistry(gameModeRegistry);
        matchEngine.setCountdown(matchCountdown);

        EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager);
        getCommand("emaraleague").setExecutor(command);
        getCommand("emaraleague").setTabCompleter(command);

        PlayerEventListener listener = new PlayerEventListener(matchEngine, playerSessionManager, command.getMessages(), winConditionEvaluator);
        getServer().getPluginManager().registerEvents(listener, this);

        getLogger().info("EmaraLeague enabled");
    }

    @Override
    public void onDisable() {
        if (tournamentRepository != null) {
            tournamentRepository.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
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

    public GameModeRegistry getGameModeRegistry() {
        return gameModeRegistry;
    }
}
