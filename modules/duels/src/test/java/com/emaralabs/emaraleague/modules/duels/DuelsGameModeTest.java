package com.emaralabs.emaraleague.modules.duels;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuelsGameModeTest {

    @Test
    void testDuelsGameModeId() {
        DuelsGameMode mode = new DuelsGameMode();
        assertEquals("duels", mode.getId());
    }

    @Test
    void testDuelsGameModeDisplayName() {
        DuelsGameMode mode = new DuelsGameMode();
        assertEquals("Duels", mode.getDisplayName());
    }

    @Test
    void testDuelsGameModeMinPlayers() {
        DuelsGameMode mode = new DuelsGameMode();
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testDuelsGameModeMaxPlayers() {
        DuelsGameMode mode = new DuelsGameMode();
        assertEquals(2, mode.getMaxPlayers());
    }
}
