package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.DisconnectGraceManager;
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
    private final DisconnectGraceManager disconnectGraceManager;
    private boolean killAnnouncementsEnabled = true;
    private double fallThreshold = 0;

    public PlayerEventListener(MatchEngine matchEngine, PlayerSessionManager sessions, MessageRegistry messages, WinConditionEvaluator winEvaluator, DisconnectGraceManager disconnectGraceManager) {
        this.matchEngine = matchEngine;
        this.sessions = sessions;
        this.messages = messages;
        this.winEvaluator = winEvaluator;
        this.disconnectGraceManager = disconnectGraceManager;
    }

    public void setFallThreshold(double fallThreshold) {
        this.fallThreshold = fallThreshold;
    }

    public void setKillAnnouncementsEnabled(boolean enabled) {
        this.killAnnouncementsEnabled = enabled;
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
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Record disconnect for grace period
        if (sessions.isInMatch(playerId)) {
            Optional<UUID> matchId = sessions.getMatchId(playerId);
            if (matchId.isPresent()) {
                disconnectGraceManager.recordDisconnect(playerId, matchId.get());
                sessions.markDisconnected(playerId);
                // Don't eliminate immediately — allow rejoin within grace period
                return;
            }
        }

        handleElimination(player);
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

        // Kill announcement
        if (killAnnouncementsEnabled) {
            announceElimination(player, match);
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

    private void announceElimination(Player eliminated, Match match) {
        // Find killer (player on opposing team who got the kill, if tracked)
        String eliminatedName = eliminated.getName();
        Team eliminatedTeam = match.teamA().playerIds().contains(eliminated.getUniqueId()) ? match.teamA() : match.teamB();
        Team opposingTeam = eliminatedTeam.id().equals(match.teamA().id()) ? match.teamB() : match.teamA();

        String message = "<gray>" + eliminatedName + " was eliminated!</gray>";
        if (!opposingTeam.playerIds().isEmpty()) {
            UUID killerId = opposingTeam.playerIds().iterator().next();
            Player killer = org.bukkit.Bukkit.getPlayer(killerId);
            if (killer != null) {
                message = "<gray>" + killer.getName() + " eliminated " + eliminatedName + "!</gray>";
            }
        }

        // Broadcast to all match players
        for (UUID playerId : match.teamA().playerIds()) {
            Player p = org.bukkit.Bukkit.getPlayer(playerId);
            if (p != null && p.isOnline()) {
                p.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
            }
        }
        for (UUID playerId : match.teamB().playerIds()) {
            Player p = org.bukkit.Bukkit.getPlayer(playerId);
            if (p != null && p.isOnline()) {
                p.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
            }
        }
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
