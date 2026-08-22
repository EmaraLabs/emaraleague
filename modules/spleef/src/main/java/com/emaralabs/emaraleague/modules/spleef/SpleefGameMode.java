package com.emaralabs.emaraleague.modules.spleef;

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

    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
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
        blocksBroken.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        blocksBroken.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        blocksBroken.merge(player.getUniqueId(), 1, Integer::sum);
    }

    public int getBlocksBroken(UUID playerId) {
        return blocksBroken.getOrDefault(playerId, 0);
    }

    public void onPlayerFall(UUID playerId) {
        eliminated.add(playerId);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
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
