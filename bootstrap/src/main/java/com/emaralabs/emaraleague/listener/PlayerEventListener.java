package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;

public final class PlayerEventListener implements Listener {

    private final MatchEngine matchEngine;
    private final PlayerSessionManager sessions;
    private final MessageRegistry messages;
    private final WinConditionEvaluator winEvaluator;
    private double fallThreshold = 0;

    public PlayerEventListener(MatchEngine matchEngine, PlayerSessionManager sessions, MessageRegistry messages, WinConditionEvaluator winEvaluator) {
        this.matchEngine = matchEngine;
        this.sessions = sessions;
        this.messages = messages;
        this.winEvaluator = winEvaluator;
    }

    public void setFallThreshold(double fallThreshold) {
        this.fallThreshold = fallThreshold;
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        handleElimination(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleElimination(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!sessions.isInMatch(player.getUniqueId())) {
            return;
        }
        if (player.getLocation().getY() < fallThreshold) {
            handleFall(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!sessions.isInMatch(player.getUniqueId())) {
            return;
        }
        handleBlockBreak(event);
    }

    private void handleElimination(Player player) {
        UUID playerId = player.getUniqueId();
        if (!sessions.isInMatch(playerId)) {
            return;
        }

        Optional<UUID> matchIdOpt = sessions.getMatchId(playerId);
        if (matchIdOpt.isEmpty()) {
            return;
        }
        UUID matchId = matchIdOpt.get();

        // Get match and game mode
        Optional<Match> matchOpt = matchEngine.getMatch(matchId);
        if (matchOpt.isEmpty()) {
            sessions.clearMatch(playerId);
            return;
        }
        Match match = matchOpt.get();

        // Get game mode for this match via registry
        GameMode gameMode = null;
        Optional<com.emaralabs.emaraleague.core.game.GameModeRegistry> registryOpt = getGameModeRegistry();
        if (registryOpt.isPresent()) {
            gameMode = registryOpt.get().getMode("duels").orElse(null);
        }

        // Mark eliminated in game mode
        if (gameMode instanceof com.emaralabs.emaraleague.modules.duels.DuelsGameMode duels) {
            duels.markEliminated(playerId);
        } else if (gameMode instanceof com.emaralabs.emaraleague.modules.spleef.SpleefGameMode spleef) {
            spleef.onPlayerFall(playerId);
        } else if (gameMode instanceof com.emaralabs.emaraleague.modules.sumo.SumoGameMode sumo) {
            sumo.onPlayerFall(playerId);
        }

        // Check win condition
        if (gameMode != null && winEvaluator.isMatchOver(match, gameMode)) {
            Optional<Team> winner = winEvaluator.evaluate(match, gameMode);
            if (winner.isPresent()) {
                matchEngine.endMatch(matchId, winner.get());
            }
        }

        // Clear player session
        sessions.clearMatch(playerId);
    }

    private void handleFall(Player player) {
        UUID playerId = player.getUniqueId();
        if (!sessions.isInMatch(playerId)) {
            return;
        }
        handleElimination(player);
    }

    private void handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!sessions.isInMatch(playerId)) {
            return;
        }
        // Block break tracking is handled by game mode hooks
    }

    private Optional<com.emaralabs.emaraleague.core.game.GameModeRegistry> getGameModeRegistry() {
        // Access via plugin instance — will be injected properly
        try {
            return Optional.ofNullable(
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getGameModeRegistry()
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
