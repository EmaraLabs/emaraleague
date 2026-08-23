package com.emaralabs.emaraleague.modules.duels;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DuelsGameMode implements GameMode {

    private static final String ID = "duels";
    private static final String DISPLAY_NAME = "Duels";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;

    // Per-match state: matchId -> state map
    private final Map<UUID, Map<UUID, Integer>> matchPlayerKills = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> matchPlayerDeaths = new HashMap<>();
    private final Map<UUID, Set<UUID>> matchEliminated = new HashMap<>();
    private final Map<UUID, Map<UUID, UUID>> matchPlayerToTeam = new HashMap<>();

    private UUID currentMatchId;

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
        matchPlayerKills.put(match.id(), new HashMap<>());
        matchPlayerDeaths.put(match.id(), new HashMap<>());
        matchEliminated.put(match.id(), new HashSet<>());
        matchPlayerToTeam.put(match.id(), new HashMap<>());
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        matchPlayerKills.remove(match.id());
        matchPlayerDeaths.remove(match.id());
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

    private Map<UUID, Integer> getKillsMap() {
        return matchPlayerKills.getOrDefault(currentMatchId, new HashMap<>());
    }

    private Map<UUID, Integer> getDeathsMap() {
        return matchPlayerDeaths.getOrDefault(currentMatchId, new HashMap<>());
    }

    private Set<UUID> getEliminatedSet() {
        return matchEliminated.getOrDefault(currentMatchId, new HashSet<>());
    }

    private Map<UUID, UUID> getTeamMap() {
        return matchPlayerToTeam.getOrDefault(currentMatchId, new HashMap<>());
    }

    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        markEliminated(player.getUniqueId());
    }

    public void markEliminated(UUID playerId) {
        getEliminatedSet().add(playerId);
        getDeathsMap().merge(playerId, 1, Integer::sum);
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        markEliminated(event.getPlayer().getUniqueId());
    }

    public int getKills(UUID playerId) {
        return getKillsMap().getOrDefault(playerId, 0);
    }

    public int getDeaths(UUID playerId) {
        return getDeathsMap().getOrDefault(playerId, 0);
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
