package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerEventListenerTest {

    private MatchEngine matchEngine;
    private PlayerSessionManager sessions;
    private MessageRegistry messages;
    private WinConditionEvaluator winEvaluator;
    private PlayerEventListener listener;

    @BeforeEach
    void setUp() {
        matchEngine = mock(MatchEngine.class);
        sessions = new PlayerSessionManager();
        messages = mock(MessageRegistry.class);
        winEvaluator = mock(WinConditionEvaluator.class);
        listener = new PlayerEventListener(matchEngine, sessions, messages, winEvaluator);
    }

    @Test
    void onPlayerDeath_notInMatch_ignores() {
        UUID playerId = UUID.randomUUID();
        sessions.createSession(playerId, "Steve");

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        Player player = mock(Player.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);

        listener.onPlayerDeath(event);

        assertTrue(sessions.getSession(playerId).isPresent());
    }

    @Test
    void onPlayerDeath_inMatch_clearsMatch() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        sessions.createSession(playerId, "Steve");
        sessions.assignToTeam(playerId, teamId);
        assertTrue(sessions.isInMatch(playerId));

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        Player player = mock(Player.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);

        listener.onPlayerDeath(event);

        assertFalse(sessions.isInMatch(playerId));
    }

    @Test
    void onPlayerDeath_nonPlayer_ignores() {
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        org.bukkit.entity.Zombie zombie = mock(org.bukkit.entity.Zombie.class);
        when(event.getEntity()).thenReturn(zombie);

        listener.onPlayerDeath(event);
        // No exception, no session changes
    }

    @Test
    void onPlayerQuit_inMatch_clearsMatch() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        sessions.createSession(playerId, "Steve");
        sessions.assignToTeam(playerId, teamId);
        assertTrue(sessions.isInMatch(playerId));

        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        Player player = mock(Player.class);
        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);

        listener.onPlayerQuit(event);

        assertFalse(sessions.isInMatch(playerId));
    }

    @Test
    void onPlayerQuit_notInMatch_ignores() {
        UUID playerId = UUID.randomUUID();
        sessions.createSession(playerId, "Steve");

        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        Player player = mock(Player.class);
        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);

        listener.onPlayerQuit(event);

        assertTrue(sessions.getSession(playerId).isPresent());
    }

    @Test
    void onPlayerQuit_noSession_ignores() {
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        Player player = mock(Player.class);
        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        listener.onPlayerQuit(event);
        // No exception
    }

    @Test
    void onPlayerDeath_inMatch_triggersWinCheck() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        sessions.createSession(playerId, "Steve");
        sessions.assignToTeam(playerId, teamId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        Player player = mock(Player.class);
        when(event.getEntity()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(playerId);

        listener.onPlayerDeath(event);

        verify(winEvaluator).isMatchOver(any(), any());
    }
}
