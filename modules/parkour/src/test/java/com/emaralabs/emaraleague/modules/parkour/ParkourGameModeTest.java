package com.emaralabs.emaraleague.modules.parkour;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParkourGameModeTest {

    @Test
    void testParkourGameModeId() {
        ParkourGameMode mode = new ParkourGameMode();
        assertEquals("parkour", mode.getId());
    }

    @Test
    void testParkourGameModeDisplayName() {
        ParkourGameMode mode = new ParkourGameMode();
        assertEquals("Parkour", mode.getDisplayName());
    }

    @Test
    void testParkourGameModePlayers() {
        ParkourGameMode mode = new ParkourGameMode();
        assertEquals(1, mode.getMinPlayers());
        assertEquals(8, mode.getMaxPlayers());
    }
}
