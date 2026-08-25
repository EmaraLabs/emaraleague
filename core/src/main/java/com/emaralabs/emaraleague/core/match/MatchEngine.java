package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.arena.Arena;
import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.arena.ArenaResetService;
import com.emaralabs.emaraleague.core.arena.ArenaState;
import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.BracketGenerator;
import com.emaralabs.emaraleague.core.game.GameModeRegistry;
import com.emaralabs.emaraleague.core.player.InventoryManager;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.player.PlayerStats;
import com.emaralabs.emaraleague.core.player.SpectatorManager;
import com.emaralabs.emaraleague.core.teleport.TeleportService;
import com.emaralabs.emaraleague.core.tournament.*;
import com.emaralabs.emaraleague.core.ui.EmaraScoreboard;
import com.emaralabs.emaraleague.core.ui.MatchAnnouncer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchEngine {

    private final TournamentManager tournaments;
    private final ArenaManager arenas;
    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> matchToTournament = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> matchToArena = new ConcurrentHashMap<>();
    private GameModeRegistry gameModeRegistry;
    private MatchCountdown countdown;
    private TeleportService teleportService;
    private PlayerSessionManager playerSessions;
    private BracketGenerator bracketGenerator;
    private EmaraScoreboard scoreboard;
    private MatchAnnouncer announcer;
    private ArenaResetService arenaResetService;
    private SpectatorManager spectatorManager;
    private PlayerStats playerStats;
    private MatchTimeout matchTimeout;
    private InventoryManager inventoryManager;
    private final Map<UUID, MatchRecord> matchHistory = new ConcurrentHashMap<>();
    private int maxConcurrentMatches = 4;

    public MatchEngine(TournamentManager tournaments, ArenaManager arenas) {
        this.tournaments = tournaments;
        this.arenas = arenas;
    }

    public void setMaxConcurrentMatches(int maxConcurrentMatches) {
        this.maxConcurrentMatches = maxConcurrentMatches;
    }

    public void setGameModeRegistry(GameModeRegistry registry) {
        this.gameModeRegistry = registry;
    }

    public void setCountdown(MatchCountdown countdown) {
        this.countdown = countdown;
    }

    public void setTeleportService(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    public void setPlayerSessionManager(PlayerSessionManager playerSessions) {
        this.playerSessions = playerSessions;
    }

    public void setBracketGenerator(BracketGenerator bracketGenerator) {
        this.bracketGenerator = bracketGenerator;
    }

    public void setScoreboard(EmaraScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void setAnnouncer(MatchAnnouncer announcer) {
        this.announcer = announcer;
    }

    public void setArenaResetService(ArenaResetService arenaResetService) {
        this.arenaResetService = arenaResetService;
    }

    public void setSpectatorManager(SpectatorManager spectatorManager) {
        this.spectatorManager = spectatorManager;
    }

    public void setPlayerStats(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public void setMatchTimeout(MatchTimeout matchTimeout) {
        this.matchTimeout = matchTimeout;
    }

    public void setInventoryManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public Map<UUID, MatchRecord> getMatchHistory() {
        return Map.copyOf(matchHistory);
    }

    public Map<UUID, UUID> getMatchToArena() {
        return Map.copyOf(matchToArena);
    }

    public int getGlobalActiveMatchCount() {
        return (int) matches.values().stream()
                .filter(m -> m.state() == MatchState.STARTING || m.state() == MatchState.INGAME)
                .count();
    }

    public Match createMatch(String tournamentName, Team teamA, Team teamB) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

        Match match = new Match(teamA, teamB);
        matches.put(match.id(), match);
        matchToTournament.put(match.id(), tournament.id());
        return match;
    }

    public Match startMatch(UUID matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        // Enforce max concurrent matches
        if (getGlobalActiveMatchCount() >= maxConcurrentMatches) {
            throw new IllegalStateException("Maximum concurrent matches reached (" + maxConcurrentMatches + "). Please wait for a match to finish.");
        }

        Match updated;
        // Synchronized validation to prevent race condition
        synchronized (matches) {
            validateMatchTransition(match.state(), MatchState.STARTING);
            updated = match.withState(MatchState.STARTING);
            matches.put(matchId, updated);

            // Auto-assign arena atomically
            assignArenaToMatch(matchId);

            // Assign players to match session
            assignPlayersToMatch(matchId);
        }

        if (gameModeRegistry != null) {
            UUID tournamentId = matchToTournament.get(matchId);
            tournaments.getTournament(tournamentId).ifPresent(t ->
                    gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchStart(updated)));
        }

        // Teleport players to arena
        teleportPlayersToArena(matchId);

        // Save and clear player inventories
        if (inventoryManager != null) {
            for (UUID playerId : match.teamA().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    inventoryManager.saveAndClearInventory(player);
                }
            }
            for (UUID playerId : match.teamB().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    inventoryManager.saveAndClearInventory(player);
                }
            }
        }

        // Start timeout timer
        if (matchTimeout != null) {
            matchTimeout.startTimer(matchId);
        }

        // Show scoreboard to players
        showScoreboardToPlayers(matchId, updated);

        if (countdown != null) {
            countdown.startCountdown(updated, 10, () -> {
                beginPlay(matchId);
                // Update scoreboard to show INGAME state
                if (scoreboard != null) {
                    Match updatedMatch = matches.get(matchId);
                    if (updatedMatch != null) {
                        scoreboard.updateAll(updatedMatch);
                    }
                }
            });
        }

        return updated;
    }

    private void assignPlayersToMatch(UUID matchId) {
        if (playerSessions == null) {
            return;
        }
        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            Match match = matches.get(matchId);
            if (match == null) {
                return;
            }
            for (UUID playerId : match.teamA().playerIds()) {
                playerSessions.assignToMatch(playerId, matchId);
                playerSessions.assignToTeam(playerId, match.teamA().id());
            }
            for (UUID playerId : match.teamB().playerIds()) {
                playerSessions.assignToMatch(playerId, matchId);
                playerSessions.assignToTeam(playerId, match.teamB().id());
            }
        });
    }

    private void showScoreboardToPlayers(UUID matchId, Match match) {
        if (scoreboard == null || playerSessions == null) {
            return;
        }
        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            for (Team team : t.teams()) {
                for (UUID playerId : team.playerIds()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        scoreboard.showToPlayer(player, match);
                    }
                }
            }
        });
    }

    private void announceMatchStartToPlayers(UUID matchId) {
        if (announcer == null) {
            return;
        }
        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            for (Team team : t.teams()) {
                for (UUID playerId : team.playerIds()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        announcer.announceMatchStart(player);
                    }
                }
            }
        });
    }

    private void announceResultToPlayers(UUID matchId, Team winner) {
        if (announcer == null) {
            return;
        }
        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            for (Team team : t.teams()) {
                for (UUID playerId : team.playerIds()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        if (team.id().equals(winner.id())) {
                            announcer.announceVictory(player, winner);
                        } else {
                            announcer.announceDefeat(player, team);
                        }
                    }
                }
            }
        });
    }

    private void assignArenaToMatch(UUID matchId) {
        List<Arena> available = arenas.getAvailableArenas();
        if (available.isEmpty()) {
            return;
        }
        Arena arena = available.get(0);
        arenas.transitionArena(arena.getName(), ArenaState.STARTING);
        matchToArena.put(matchId, arena.getId());
    }

    private void teleportPlayersToArena(UUID matchId) {
        if (teleportService == null || playerSessions == null) {
            return;
        }
        UUID arenaId = matchToArena.get(matchId);
        if (arenaId == null) {
            return;
        }
        Optional<Arena> arena = arenas.getArena(arenaId);
        if (arena.isEmpty() || arena.get().getCenter() == null) {
            return;
        }

        Match match = matches.get(matchId);
        if (match == null) {
            return;
        }

        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }

        tournaments.getTournament(tournamentId).ifPresent(t -> {
            // Team A to spawnA, Team B to spawnB
            Location spawnA = arena.get().getSpawnA();
            Location spawnB = arena.get().getSpawnB();

            for (UUID playerId : match.teamA().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.teleport(spawnA);
                }
            }
            for (UUID playerId : match.teamB().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.teleport(spawnB);
                }
            }
        });
    }

    public Match beginPlay(UUID matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        // Idempotent — if already INGAME (e.g. countdown fired twice), return silently
        if (match.state() == MatchState.INGAME) {
            return match;
        }
        validateMatchTransition(match.state(), MatchState.INGAME);
        Match updated = match.withState(MatchState.INGAME);
        matches.put(matchId, updated);

        // Announce match start to players
        announceMatchStartToPlayers(matchId);

        // Transition arena to INGAME
        UUID arenaId = matchToArena.get(matchId);
        if (arenaId != null) {
            arenas.getArena(arenaId).ifPresent(arena -> {
                if (arena.getState() == ArenaState.STARTING) {
                    arenas.transitionArena(arena.getName(), ArenaState.INGAME);
                }
            });
        }

        return updated;
    }

    public Match endMatch(UUID matchId, Team winner) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        // Idempotent — if already ENDED (e.g. cancel called twice), return silently
        if (match.state() == MatchState.ENDED) {
            return match;
        }
        validateMatchTransition(match.state(), MatchState.ENDED);
        Match updated = match.withWinner(winner);
        matches.put(matchId, updated);

        // Transition arena to ENDING, then reset to LOBBY
        UUID arenaId = matchToArena.get(matchId);
        if (arenaId != null) {
            arenas.getArena(arenaId).ifPresent(arena -> {
                if (arena.getState() == ArenaState.INGAME) {
                    arenas.transitionArena(arena.getName(), ArenaState.ENDING);
                    arenas.transitionArena(arena.getName(), ArenaState.RESETTING);

                    // Restore arena blocks (Spleef, etc.)
                    if (arenaResetService != null && arenaResetService.hasTrackedChanges(arena)) {
                        arenaResetService.restoreArena(arena);
                    }

                    arenas.transitionArena(arena.getName(), ArenaState.LOBBY);
                }
            });
        }

        // Restore player inventories
        if (inventoryManager != null) {
            for (UUID playerId : match.teamA().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    inventoryManager.restoreInventory(player);
                }
            }
            for (UUID playerId : match.teamB().playerIds()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    inventoryManager.restoreInventory(player);
                }
            }
        }

        // Cancel timeout timer
        if (matchTimeout != null) {
            matchTimeout.cancelTimer(matchId);
        }

        // Cancel countdown if running
        if (countdown != null) {
            try {
                countdown.cancel();
            } catch (Exception ignored) {
                // Countdown already finished or cancelled
            }
        }

        // Teleport players back to lobby
        teleportPlayersToLobby(matchId);

        // Hide scoreboard
        if (scoreboard != null) {
            scoreboard.hideFromAll();
        }

        // Announce result to players
        announceResultToPlayers(matchId, winner);

        // Record match history
        UUID historyTournamentId = matchToTournament.get(matchId);
        if (historyTournamentId != null) {
            tournaments.getTournament(historyTournamentId).ifPresent(t -> {
                MatchRecord record = MatchRecord.fromMatch(updated, t.name(), t.mode());
                matchHistory.put(matchId, record);
            });
        }

        // Update player stats
        if (playerStats != null && historyTournamentId != null) {
            tournaments.getTournament(historyTournamentId).ifPresent(t -> {
                for (Team team : t.teams()) {
                    for (UUID playerId : team.playerIds()) {
                        if (team.id().equals(winner.id())) {
                            playerStats.addWin(playerId);
                        } else {
                            playerStats.addLoss(playerId);
                        }
                    }
                }
            });
        }

        // Clear spectators
        if (spectatorManager != null) {
            spectatorManager.clearMatchSpectators(matchId);
        }

        if (gameModeRegistry != null) {
            UUID tournamentId = matchToTournament.get(matchId);
            tournaments.getTournament(tournamentId).ifPresent(t ->
                    gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchEnd(updated, winner)));
        }

        // Auto-advance bracket and start next match
        if (bracketGenerator != null) {
            UUID tournamentId = matchToTournament.get(matchId);
            tournaments.getTournament(tournamentId).ifPresent(t -> {
                Bracket currentBracket = new Bracket(getMatches(t.name()));
                Bracket advanced = bracketGenerator.advance(currentBracket, updated);

                // Store newly populated matches
                for (Match m : advanced.getMatches()) {
                    if (!matches.containsKey(m.id()) && m.teamA() != null && m.teamB() != null) {
                        matches.put(m.id(), m);
                        matchToTournament.put(m.id(), tournamentId);
                    }
                }

                // Auto-start next match if available
                getNextMatch(t.name()).ifPresent(next -> {
                    if (next.state() == MatchState.PENDING) {
                        startMatch(next.id());
                    }
                });
            });
        }

        return updated;
    }

    public boolean isTournamentComplete(String tournamentName) {
        return getMatches(tournamentName).stream()
                .allMatch(m -> m.state() == MatchState.ENDED);
    }

    public Optional<Team> getChampion(String tournamentName) {
        if (!isTournamentComplete(tournamentName)) {
            return Optional.empty();
        }
        return getMatches(tournamentName).stream()
                .filter(m -> m.state() == MatchState.ENDED && m.winner() != null)
                .map(Match::winner)
                .findFirst();
    }

    private void teleportPlayersToLobby(UUID matchId) {
        if (teleportService == null || playerSessions == null) {
            return;
        }
        UUID arenaId = matchToArena.get(matchId);
        if (arenaId == null) {
            return;
        }
        Optional<Arena> arena = arenas.getArena(arenaId);
        if (arena.isEmpty() || arena.get().getLobbySpawn() == null) {
            return;
        }

        Match match = matches.get(matchId);
        if (match == null) {
            return;
        }

        UUID tournamentId = matchToTournament.get(matchId);
        if (tournamentId == null) {
            return;
        }

        tournaments.getTournament(tournamentId).ifPresent(t -> {
            for (Team team : t.teams()) {
                for (UUID playerId : team.playerIds()) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        teleportService.teleportToLobby(player);
                    }
                }
            }
        });
    }

    public Optional<Match> getMatch(UUID matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    public List<Match> getMatches(String tournamentName) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));
        return matches.values().stream()
                .filter(m -> tournament.id().equals(matchToTournament.get(m.id())))
                .toList();
    }

    public List<Match> getMatchesByState(String tournamentName, MatchState state) {
        return getMatches(tournamentName).stream()
                .filter(m -> m.state() == state)
                .toList();
    }

    public int getActiveMatchCount(String tournamentName) {
        return (int) getMatches(tournamentName).stream()
                .filter(m -> m.state() == MatchState.STARTING || m.state() == MatchState.INGAME)
                .count();
    }

    public Bracket generateBracket(String tournamentName, BracketGenerator generator) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

        if (tournament.teams().size() < 2) {
            throw new IllegalStateException("Need at least 2 teams to generate bracket");
        }

        Bracket bracket = generator.generate(tournament.teams());

        for (Match match : bracket.getMatches()) {
            if (match.teamA() != null && match.teamB() != null) {
                matches.put(match.id(), match);
                matchToTournament.put(match.id(), tournament.id());
            }
        }

        return bracket;
    }

    public Optional<Match> getNextMatch(String tournamentName) {
        return getMatchesByState(tournamentName, MatchState.PENDING).stream()
                .findFirst();
    }

    private void validateMatchTransition(MatchState current, MatchState next) {
        boolean valid = switch (current) {
            case PENDING -> next == MatchState.STARTING;
            case STARTING -> next == MatchState.INGAME;
            case INGAME -> next == MatchState.ENDED;
            case ENDED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid match transition: %s -> %s", current, next)
            );
        }
    }
}
