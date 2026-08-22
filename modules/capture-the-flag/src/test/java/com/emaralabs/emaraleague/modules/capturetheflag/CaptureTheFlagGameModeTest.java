package com.emaralabs.emaraleague.modules.capturetheflag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CaptureTheFlagGameModeTest {

    @Test
    void testCTFGameModeId() {
        CaptureTheFlagGameMode mode = new CaptureTheFlagGameMode();
        assertEquals("ctf", mode.getId());
    }

    @Test
    void testCTFGameModeDisplayName() {
        CaptureTheFlagGameMode mode = new CaptureTheFlagGameMode();
        assertEquals("Capture The Flag", mode.getDisplayName());
    }

    @Test
    void testCTFGameModePlayers() {
        CaptureTheFlagGameMode mode = new CaptureTheFlagGameMode();
        assertEquals(4, mode.getMinPlayers());
        assertEquals(16, mode.getMaxPlayers());
    }
}
