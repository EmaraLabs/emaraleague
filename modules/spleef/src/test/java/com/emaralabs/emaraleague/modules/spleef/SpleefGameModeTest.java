package com.emaralabs.emaraleague.modules.spleef;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpleefGameModeTest {

    @Test
    void testSpleefGameModeId() {
        SpleefGameMode mode = new SpleefGameMode();
        assertEquals("spleef", mode.getId());
    }

    @Test
    void testSpleefGameModeDisplayName() {
        SpleefGameMode mode = new SpleefGameMode();
        assertEquals("Spleef", mode.getDisplayName());
    }

    @Test
    void testSpleefGameModeMinPlayers() {
        SpleefGameMode mode = new SpleefGameMode();
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testSpleefGameModeMaxPlayers() {
        SpleefGameMode mode = new SpleefGameMode();
        assertEquals(16, mode.getMaxPlayers());
    }
}
