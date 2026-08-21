package com.emaralabs.emaraleague.core.player;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PlayerSessionTest {

    @Test
    void testPlayerSessionCreation() {
        UUID playerId = UUID.randomUUID();
        PlayerSession session = new PlayerSession(playerId, "player1");
        assertEquals(playerId, session.getPlayerId());
        assertEquals("player1", session.getPlayerName());
        assertTrue(session.isActive());
    }

    @Test
    void testPlayerSessionDeactivate() {
        UUID playerId = UUID.randomUUID();
        PlayerSession session = new PlayerSession(playerId, "player1");
        session.deactivate();
        assertFalse(session.isActive());
    }

    @Test
    void testPlayerSessionSpectatorMode() {
        UUID playerId = UUID.randomUUID();
        PlayerSession session = new PlayerSession(playerId, "player1");
        assertFalse(session.isSpectator());
        session.setSpectator(true);
        assertTrue(session.isSpectator());
    }

    @Test
    void testPlayerSessionTeamAssignment() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        PlayerSession session = new PlayerSession(playerId, "player1");
        assertNull(session.getTeamId());
        session.setTeamId(teamId);
        assertEquals(teamId, session.getTeamId());
    }

    @Test
    void testPlayerSessionStats() {
        UUID playerId = UUID.randomUUID();
        PlayerSession session = new PlayerSession(playerId, "player1");
        
        assertEquals(0, session.getKills());
        assertEquals(0, session.getDeaths());
        assertEquals(0, session.getWins());
        assertEquals(0, session.getLosses());
        
        session.addKill();
        session.addKill();
        session.addDeath();
        session.addWin();
        
        assertEquals(2, session.getKills());
        assertEquals(1, session.getDeaths());
        assertEquals(1, session.getWins());
        assertEquals(0, session.getLosses());
    }
}
