package com.emaralabs.emaraleague.modules.spleef;

import com.emaralabs.emaraleague.core.arena.ArenaResetService;
import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SpleefGameMode implements GameMode {

    private static final String ID = "spleef";
    private static final String DISPLAY_NAME = "Spleef";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 16;

    // Per-match state: matchId -> state map
    private final Map<UUID, Map<UUID, Integer>> matchBlocksBroken = new HashMap<>();
    private final Map<UUID, Set<UUID>> matchEliminated = new HashMap<>();
    private final Map<UUID, Map<UUID, UUID>> matchPlayerToTeam = new HashMap<>();

    private UUID currentMatchId;
    private ArenaResetService arenaResetService;
    private com.emaralabs.emaraleague.core.arena.Arena currentArena;

    public void setArenaResetService(ArenaResetService service) {
        this.arenaResetService = service;
    }

    public void setCurrentArena(com.emaralabs.emaraleague.core.arena.Arena arena) {
        this.currentArena = arena;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }

    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    @Override
    public void onMatchStart(Match match) {
        currentMatchId = match.id();
        matchBlocksBroken.put(match.id(), new HashMap<>());
        matchEliminated.put(match.id(), new HashSet<>());
        matchPlayerToTeam.put(match.id(), new HashMap<>());
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        matchBlocksBroken.remove(match.id());
        matchEliminated.remove(match.id());
        matchPlayerToTeam.remove(match.id());
        if (match.id().equals(currentMatchId)) {
            currentMatchId = null;
        }
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    private Map<UUID, Integer> getBlocksMap() {
        return matchBlocksBroken.getOrDefault(currentMatchId, new HashMap<>());
    }

    private Set<UUID> getEliminatedSet() {
        return matchEliminated.getOrDefault(currentMatchId, new HashSet<>());
    }

    private Map<UUID, UUID> getTeamMap() {
        return matchPlayerToTeam.getOrDefault(currentMatchId, new HashMap<>());
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        getBlocksMap().merge(player.getUniqueId(), 1, Integer::sum);

        // Track for arena reset
        if (arenaResetService != null && currentArena != null) {
            arenaResetService.trackBlockBreak(currentArena, event.getBlock());
        }
    }

    public int getBlocksBroken(UUID playerId) {
        return getBlocksMap().getOrDefault(playerId, 0);
    }

    public void onPlayerFall(UUID playerId) {
        getEliminatedSet().add(playerId);
    }

    public boolean isEliminated(UUID playerId) {
        return getEliminatedSet().contains(playerId);
    }

    public int getAliveCount(Match match) {
        return 2 - getEliminatedSet().size();
    }

    public void assignPlayerToTeam(UUID playerId, UUID teamId) {
        getTeamMap().put(playerId, teamId);
    }

    public Optional<UUID> getTeamForPlayer(UUID playerId) {
        return Optional.ofNullable(getTeamMap().get(playerId));
    }

    public boolean isTeamEliminated(UUID teamId) {
        return getTeamMap().entrySet().stream()
                .filter(e -> e.getValue().equals(teamId))
                .allMatch(e -> getEliminatedSet().contains(e.getKey()));
    }
}
