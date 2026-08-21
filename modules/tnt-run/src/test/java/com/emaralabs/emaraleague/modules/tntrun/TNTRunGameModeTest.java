package com.emaralabs.emaraleague.modules.tntrun;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TNTRunGameModeTest {

    @Test
    void testTNTRunGameModeId() {
        TNTRunGameMode mode = new TNTRunGameMode();
        assertEquals("tnt-run", mode.getId());
    }

    @Test
    void testTNTRunGameModeDisplayName() {
        TNTRunGameMode mode = new TNTRunGameMode();
        assertEquals("TNT Run", mode.getDisplayName());
    }

    @Test
    void testTNTRunGameModeMinPlayers() {
        TNTRunGameMode mode = new TNTRunGameMode();
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testTNTRunGameModeMaxPlayers() {
        TNTRunGameMode mode = new TNTRunGameMode();
        assertEquals(16, mode.getMaxPlayers());
    }
}
