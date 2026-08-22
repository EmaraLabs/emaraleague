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

    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<UUID, Integer> playerDeaths = new HashMap<>();
    private final Set<UUID> eliminated = new HashSet<>();
    private final Map<UUID, UUID> playerToTeam = new HashMap<>();

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
        playerKills.clear();
        playerDeaths.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        playerKills.clear();
        playerDeaths.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID id = player.getUniqueId();
            playerDeaths.merge(id, 1, Integer::sum);
            eliminated.add(id);
        }
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        playerDeaths.merge(id, 1, Integer::sum);
        eliminated.add(id);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
    }

    public int getAliveCount(Match match) {
        return 2 - eliminated.size();
    }

    public int getKills(UUID playerId) {
        return playerKills.getOrDefault(playerId, 0);
    }

    public int getDeaths(UUID playerId) {
        return playerDeaths.getOrDefault(playerId, 0);
    }

    public void assignPlayerToTeam(UUID playerId, UUID teamId) {
        playerToTeam.put(playerId, teamId);
    }

    public Optional<UUID> getTeamForPlayer(UUID playerId) {
        return Optional.ofNullable(playerToTeam.get(playerId));
    }

    public boolean isTeamEliminated(UUID teamId) {
        return playerToTeam.entrySet().stream()
                .filter(e -> e.getValue().equals(teamId))
                .allMatch(e -> eliminated.contains(e.getKey()));
    }
}
