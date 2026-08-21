package com.emaralabs.emaraleague.core.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameModeTest {

    @Test
    void testGameModeInterface() {
        GameMode mode = new TestGameMode();
        assertEquals("test", mode.getId());
        assertEquals("Test Mode", mode.getDisplayName());
        assertEquals(2, mode.getMinPlayers());
        assertEquals(4, mode.getMaxPlayers());
    }

    @Test
    void testWinCondition() {
        GameMode mode = new TestGameMode();
        assertEquals(WinCondition.LAST_TEAM_STANDING, mode.getWinCondition());
    }

    private static class TestGameMode implements GameMode {
        @Override
        public String getId() { return "test"; }

        @Override
        public String getDisplayName() { return "Test Mode"; }

        @Override
        public int getMinPlayers() { return 2; }

        @Override
        public int getMaxPlayers() { return 4; }

        @Override
        public void onMatchStart(com.emaralabs.emaraleague.core.tournament.Match match) {}

        @Override
        public void onMatchTick(com.emaralabs.emaraleague.core.tournament.Match match) {}

        @Override
        public void onMatchEnd(com.emaralabs.emaraleague.core.tournament.Match match, com.emaralabs.emaraleague.core.tournament.Team winner) {}

        @Override
        public WinCondition getWinCondition() { return WinCondition.LAST_TEAM_STANDING; }
    }
}
