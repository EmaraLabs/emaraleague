package com.emaralabs.emaraleague;

import com.emaralabs.emaraleague.api.EmaraAddon;
import com.emaralabs.emaraleague.api.EmaraLeagueAPI;
import com.emaralabs.emaraleague.command.EmaraLeagueCommand;
import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.arena.ArenaPersistence;
import com.emaralabs.emaraleague.core.arena.ArenaResetService;
import com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket;
import com.emaralabs.emaraleague.core.game.GameModeRegistry;
import com.emaralabs.emaraleague.core.match.MatchCountdown;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.MatchHistoryPersistence;
import com.emaralabs.emaraleague.core.match.MatchTimeout;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.DisconnectGraceManager;
import com.emaralabs.emaraleague.core.player.InventoryManager;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.player.PlayerStatsPersistence;
import com.emaralabs.emaraleague.core.scheduler.PaperScheduler;
import com.emaralabs.emaraleague.core.teleport.TeleportService;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import com.emaralabs.emaraleague.core.ui.MatchAnnouncer;
import com.emaralabs.emaraleague.core.ui.MatchScoreboard;
import com.emaralabs.emaraleague.infrastructure.database.DatabaseManager;
import com.emaralabs.emaraleague.infrastructure.database.TournamentRepository;
import com.emaralabs.emaraleague.listener.PlayerEventListener;
import com.emaralabs.emaraleague.modules.duels.DuelsGameMode;
import com.emaralabs.emaraleague.modules.spleef.SpleefGameMode;
import com.emaralabs.emaraleague.modules.sumo.SumoGameMode;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EmaraLeaguePlugin extends JavaPlugin implements EmaraLeagueAPI {

    private static final int API_VERSION = 1;
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
    private ArenaPersistence arenaPersistence;
    private MatchHistoryPersistence matchHistoryPersistence;
    private PlayerStatsPersistence playerStatsPersistence;
    private DisconnectGraceManager disconnectGraceManager;
    private com.emaralabs.emaraleague.core.player.PlayerStats playerStats;
    private final Map<String, EmaraAddon> addons = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Ensure data folder exists before database init
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            // Copy default config.yml if not exists
            saveDefaultConfig();

            String dbPath = getDataFolder().getAbsolutePath() + "/emaraleague.db";
            databaseManager = new DatabaseManager("jdbc:sqlite:" + dbPath, "", "");
            databaseManager.initializeSchema();
            tournamentRepository = new TournamentRepository(databaseManager);

            tournamentManager = new TournamentManager();
            tournamentManager.setPersistence(tournamentRepository);
            tournamentManager.loadFromDatabase();

            arenaManager = new ArenaManager();
            arenaPersistence = new ArenaPersistence(this);
            arenaPersistence.load(arenaManager);

            playerSessionManager = new PlayerSessionManager();
            teleportService = new TeleportService();
            matchEngine = new MatchEngine(tournamentManager, arenaManager);
            matchEngine.setTeleportService(teleportService);
            matchEngine.setPlayerSessionManager(playerSessionManager);
            gameModeRegistry = new GameModeRegistry();
            winConditionEvaluator = new WinConditionEvaluator(playerSessionManager);
            matchCountdown = new MatchCountdown(new PaperScheduler(this), null);

            matchHistoryPersistence = new MatchHistoryPersistence(this);
            playerStatsPersistence = new PlayerStatsPersistence(this);

            playerStats = new com.emaralabs.emaraleague.core.player.PlayerStats();
            playerStatsPersistence.load(playerStats);
            matchEngine.setPlayerStats(playerStats);

            MatchScoreboard scoreboard = new MatchScoreboard(matchEngine);
            matchEngine.setScoreboard(scoreboard);

            MatchAnnouncer announcer = new MatchAnnouncer();
            matchEngine.setAnnouncer(announcer);

            ArenaResetService arenaResetService = new ArenaResetService();
            matchEngine.setArenaResetService(arenaResetService);

            MatchTimeout matchTimeout = new MatchTimeout(new PaperScheduler(this), matchEngine);
            matchEngine.setMatchTimeout(matchTimeout);

            InventoryManager inventoryManager = new InventoryManager();
            matchEngine.setInventoryManager(inventoryManager);

            disconnectGraceManager = new DisconnectGraceManager();

            gameModeRegistry.register(new DuelsGameMode());
            gameModeRegistry.register(new SpleefGameMode());
            gameModeRegistry.register(new SumoGameMode());
            matchEngine.setGameModeRegistry(gameModeRegistry);
            matchEngine.setCountdown(matchCountdown);
            matchEngine.setBracketGenerator(new SingleEliminationBracket());

            EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager, arenaManager);

            BasicCommand basicCommand = new BasicCommand() {
                @Override
                public void execute(CommandSourceStack stack, String[] args) {
                    command.onCommand(stack.getSender(), null, "emaraleague", args);
                }

                @Override
                public List<String> suggest(CommandSourceStack stack, String[] args) {
                    return command.onTabComplete(stack.getSender(), null, "emaraleague", args);
                }
            };

            registerCommand("emaraleague", "EmaraLeague tournament management", List.of("el", "league"), basicCommand);

            PlayerEventListener listener = new PlayerEventListener(matchEngine, playerSessionManager, command.getMessages(), winConditionEvaluator, disconnectGraceManager);
            getServer().getPluginManager().registerEvents(listener, this);

            getLogger().info("EmaraLeague enabled");
        } catch (Exception e) {
            getLogger().severe("Failed to enable EmaraLeague: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Disable all addons first
        for (EmaraAddon addon : List.copyOf(addons.values())) {
            unregisterAddon(addon.getId());
        }

        // Save persistence data
        if (arenaPersistence != null) {
            arenaPersistence.save(arenaManager);
        }
        if (matchHistoryPersistence != null && matchEngine != null) {
            matchHistoryPersistence.save(List.copyOf(matchEngine.getMatchHistory().values()));
        }

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

    public MatchHistoryPersistence getMatchHistoryPersistence() {
        return matchHistoryPersistence;
    }

    public PlayerStatsPersistence getPlayerStatsPersistence() {
        return playerStatsPersistence;
    }

    public com.emaralabs.emaraleague.core.player.PlayerStats getPlayerStats() {
        return playerStats;
    }

    // ── EmaraLeagueAPI Implementation ───────────────────────────────

    public EmaraLeagueAPI getAPI() {
        return this;
    }

    @Override
    public int getApiVersion() {
        return API_VERSION;
    }

    @Override
    public void registerAddon(EmaraAddon addon) {
        if (addon.getRequiredApiVersion() > API_VERSION) {
            throw new IllegalArgumentException(
                addon.getName() + " requires API v" + addon.getRequiredApiVersion() +
                " but current is v" + API_VERSION
            );
        }
        addons.put(addon.getId(), addon);
        addon.onEnable(this);
        getLogger().info("Addon enabled: " + addon.getName() + " v" + addon.getVersion());
    }

    @Override
    public void unregisterAddon(String addonId) {
        EmaraAddon addon = addons.remove(addonId);
        if (addon != null) {
            addon.onDisable();
            getLogger().info("Addon disabled: " + addon.getName());
        }
    }

    @Override
    public List<EmaraAddon> getAddons() {
        return List.copyOf(addons.values());
    }

    @Override
    public boolean isAddonEnabled(String addonId) {
        return addons.containsKey(addonId);
    }

    @Override
    public void registerGameMode(Object gameMode) {
        if (gameMode instanceof com.emaralabs.emaraleague.core.game.GameMode mode) {
            gameModeRegistry.register(mode);
        }
    }

    @Override
    public void unregisterGameMode(String gameModeId) {
        gameModeRegistry.unregister(gameModeId);
    }

    @Override
    public void broadcastToTournament(String tournamentName, net.kyori.adventure.text.Component message) {
        tournamentManager.getTournament(tournamentName).ifPresent(t -> {
            for (var team : t.teams()) {
                for (var playerId : team.playerIds()) {
                    var player = getServer().getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        player.sendMessage(message);
                    }
                }
            }
        });
    }

    @Override
    public void broadcastToMatch(UUID matchId, net.kyori.adventure.text.Component message) {
        matchEngine.getMatch(matchId).ifPresent(match -> {
            for (var playerId : match.teamA().playerIds()) {
                var player = getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
            for (var playerId : match.teamB().playerIds()) {
                var player = getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
        });
    }
}
