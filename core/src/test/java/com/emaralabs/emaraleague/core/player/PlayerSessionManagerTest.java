package com.emaralabs.emaraleague.core.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSessionManagerTest {

    private PlayerSessionManager manager;

    @BeforeEach
    void setUp() {
        manager = new PlayerSessionManager();
    }

    @Test
    void createSession_createsNewSession() {
        UUID id = UUID.randomUUID();
        PlayerSession session = manager.createSession(id, "Steve");
        assertNotNull(session);
        assertEquals(id, session.getPlayerId());
        assertEquals("Steve", session.getPlayerName());
        assertTrue(session.isActive());
        assertFalse(session.isSpectator());
    }

    @Test
    void getSession_existing_returnsSession() {
        UUID id = UUID.randomUUID();
        manager.createSession(id, "Steve");
        Optional<PlayerSession> found = manager.getSession(id);
        assertTrue(found.isPresent());
        assertEquals("Steve", found.get().getPlayerName());
    }

    @Test
    void getSession_notFound_returnsEmpty() {
        assertTrue(manager.getSession(UUID.randomUUID()).isEmpty());
    }

    @Test
    void removeSession_deletesSession() {
        UUID id = UUID.randomUUID();
        manager.createSession(id, "Steve");
        manager.removeSession(id);
        assertTrue(manager.getSession(id).isEmpty());
    }

    @Test
    void assignToTeam_setsTeamId() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        manager.createSession(playerId, "Steve");
        manager.assignToTeam(playerId, teamId);
        assertEquals(teamId, manager.getTeamId(playerId).orElse(null));
    }

    @Test
    void assignToTeam_nonExistentPlayer_doesNothing() {
        manager.assignToTeam(UUID.randomUUID(), UUID.randomUUID());
        // No exception thrown
    }

    @Test
    void setSpectator_marksAsSpectator() {
        UUID id = UUID.randomUUID();
        manager.createSession(id, "Steve");
        manager.setSpectator(id, true);
        assertTrue(manager.getSession(id).get().isSpectator());
    }

    @Test
    void getTeamId_withTeam_returnsTeamId() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        manager.createSession(playerId, "Steve");
        manager.assignToTeam(playerId, teamId);
        assertTrue(manager.getTeamId(playerId).isPresent());
        assertEquals(teamId, manager.getTeamId(playerId).get());
    }

    @Test
    void getTeamId_noTeam_returnsEmpty() {
        UUID id = UUID.randomUUID();
        manager.createSession(id, "Steve");
        assertTrue(manager.getTeamId(id).isEmpty());
    }

    @Test
    void isInMatch_withTeam_returnsTrue() {
        UUID playerId = UUID.randomUUID();
        manager.createSession(playerId, "Steve");
        manager.assignToTeam(playerId, UUID.randomUUID());
        assertTrue(manager.isInMatch(playerId));
    }

    @Test
    void isInMatch_noTeam_returnsFalse() {
        UUID id = UUID.randomUUID();
        manager.createSession(id, "Steve");
        assertFalse(manager.isInMatch(id));
    }

    @Test
    void isInMatch_noSession_returnsFalse() {
        assertFalse(manager.isInMatch(UUID.randomUUID()));
    }

    @Test
    void clearMatch_removesTeam() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        manager.createSession(playerId, "Steve");
        manager.assignToTeam(playerId, teamId);
        assertTrue(manager.isInMatch(playerId));
        manager.clearMatch(playerId);
        assertFalse(manager.isInMatch(playerId));
    }

    @Test
    void clearMatch_nonExistentPlayer_doesNothing() {
        manager.clearMatch(UUID.randomUUID());
        // No exception thrown
    }
}
