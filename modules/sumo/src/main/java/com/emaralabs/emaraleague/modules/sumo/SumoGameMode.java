package com.emaralabs.emaraleague.modules.sumo;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SumoGameMode implements GameMode {

    private static final String ID = "sumo";
    private static final String DISPLAY_NAME = "Sumo";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;

    private final Map<UUID, Integer> knockbacksDealt = new HashMap<>();
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
        knockbacksDealt.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        knockbacksDealt.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onKnockbackDealt(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            knockbacksDealt.merge(attacker.getUniqueId(), 1, Integer::sum);
        }
    }

    public int getKnockbacksDealt(UUID playerId) {
        return knockbacksDealt.getOrDefault(playerId, 0);
    }

    public void onPlayerFall(UUID playerId) {
        eliminated.add(playerId);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
    }

    public int getAliveCount(Match match) {
        return 2 - eliminated.size();
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
