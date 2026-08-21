package com.emaralabs.emaraleague.core.player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerSessionTest {

    @Test
    void testPlayerSessionCreation() {
        PlayerSession session = new PlayerSession("player1");
        assertEquals("player1", session.getPlayerName());
        assertTrue(session.isActive());
    }

    @Test
    void testPlayerSessionDeactivate() {
        PlayerSession session = new PlayerSession("player1");
        session.deactivate();
        assertFalse(session.isActive());
    }

    @Test
    void testPlayerSessionSpectatorMode() {
        PlayerSession session = new PlayerSession("player1");
        assertFalse(session.isSpectator());
        session.setSpectator(true);
        assertTrue(session.isSpectator());
    }
}
