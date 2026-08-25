package com.emaralabs.emaraleague.core.player;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;

/**
 * Monitors disconnected players and declares enemy team as winner
 * if the player doesn't rejoin within the grace period.
 * Broadcasts a configurable message when a player is disqualified.
 */
public final class LogoutGuardService {

    private final Plugin plugin;
    private final MatchEngine matchEngine;
    private final PlayerSessionManager sessions;
    private final DisconnectGraceManager graceManager;
    private boolean enabled = true;
    private String logoutMessage = "<red>%player% has logged out and is disqualified from the tournament!";
    private int taskId = -1;

    public LogoutGuardService(Plugin plugin, MatchEngine matchEngine, PlayerSessionManager sessions, DisconnectGraceManager graceManager) {
        this.plugin = plugin;
        this.matchEngine = matchEngine;
        this.sessions = sessions;
        this.graceManager = graceManager;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLogoutMessage(String message) {
        this.logoutMessage = message;
    }

    /**
     * Start the repeating check task (every second).
     */
    public void start() {
        if (taskId != -1) {
            return; // Already running
        }
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkExpiredPlayers, 20L, 20L);
    }

    /**
     * Stop the repeating task.
     */
    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Check all disconnected players and disqualify those whose grace period expired.
     */
    private void checkExpiredPlayers() {
        if (!enabled) {
            return;
        }

        // Iterate over a snapshot to avoid concurrent modification
        for (UUID playerId : graceManager.getDisconnectedPlayers()) {
            if (!graceManager.canRejoin(playerId)) {
                // Grace expired — disqualify
                disqualifyPlayer(playerId);
            }
        }
    }

    /**
     * Disqualify a player — enemy team wins the match.
     */
    private void disqualifyPlayer(UUID playerId) {
        UUID matchId = graceManager.getDisconnectedMatch(playerId);
        if (matchId == null) {
            return;
        }

        Optional<Match> matchOpt = matchEngine.getMatch(matchId);
        if (matchOpt.isEmpty()) {
            cleanupPlayer(playerId);
            return;
        }

        Match match = matchOpt.get();
        if (match.state() != MatchState.INGAME && match.state() != MatchState.STARTING) {
            cleanupPlayer(playerId);
            return;
        }

        // Determine enemy team (winner by default)
        Team winner = null;
        if (match.teamA().playerIds().contains(playerId)) {
            winner = match.teamB();
        } else if (match.teamB().playerIds().contains(playerId)) {
            winner = match.teamA();
        }

        // Get player name for broadcast
        String playerName = Optional.ofNullable(Bukkit.getOfflinePlayer(playerId).getName())
                .orElse("A player");

        // Broadcast disqualification message
        String broadcast = logoutMessage.replace("%player%", playerName);
        Bukkit.getServer().sendMessage(MiniMessage.miniMessage().deserialize(broadcast));

        // End match with enemy team as winner
        if (winner != null) {
            matchEngine.endMatch(matchId, winner);
        }

        // Cleanup
        cleanupPlayer(playerId);
        sessions.clearMatch(playerId);

        plugin.getLogger().info("Player " + playerName + " disqualified from match " + matchId + " (logout guard)");
    }

    /**
     * Remove player from tracking.
     */
    private void cleanupPlayer(UUID playerId) {
        graceManager.clearPlayer(playerId);
    }
}
