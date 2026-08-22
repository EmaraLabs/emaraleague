package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        sessions.clearMatch(playerId);
        winEvaluator.isMatchOver(null, null);
    }

    private void handleFall(Player player) {
        UUID playerId = player.getUniqueId();
        sessions.clearMatch(playerId);
        winEvaluator.isMatchOver(null, null);
    }

    private void handleBlockBreak(BlockBreakEvent event) {
        // Block break tracking is handled by game mode hooks
        // This listener just ensures only in-match players are tracked
    }
}
