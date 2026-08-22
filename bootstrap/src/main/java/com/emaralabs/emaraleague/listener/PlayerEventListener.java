package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerEventListener implements Listener {

    private final MatchEngine matchEngine;
    private final PlayerSessionManager sessions;
    private final MessageRegistry messages;

    public PlayerEventListener(MatchEngine matchEngine, PlayerSessionManager sessions, MessageRegistry messages) {
        this.matchEngine = matchEngine;
        this.sessions = sessions;
        this.messages = messages;
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

    private void handleElimination(Player player) {
        UUID playerId = player.getUniqueId();
        if (!sessions.isInMatch(playerId)) {
            return;
        }
        sessions.clearMatch(playerId);
    }
}
