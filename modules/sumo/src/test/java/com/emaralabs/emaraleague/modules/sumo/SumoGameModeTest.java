package com.emaralabs.emaraleague.modules.sumo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SumoGameModeTest {

    @Test
    void testSumoGameModeId() {
        SumoGameMode mode = new SumoGameMode();
        assertEquals("sumo", mode.getId());
    }

    @Test
    void testSumoGameModeDisplayName() {
        SumoGameMode mode = new SumoGameMode();
        assertEquals("Sumo", mode.getDisplayName());
    }

    @Test
    void testSumoGameModeMinPlayers() {
        SumoGameMode mode = new SumoGameMode();
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testSumoGameModeMaxPlayers() {
        SumoGameMode mode = new SumoGameMode();
        assertEquals(2, mode.getMaxPlayers());
    }
}
