package com.emaralabs.emaraleague.core.arena;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArenaTest {

    @Test
    void testArenaInitialState() {
        Arena arena = new Arena("test-arena");
        assertEquals(ArenaState.LOBBY, arena.getState());
    }

    @Test
    void testArenaStateTransition() {
        Arena arena = new Arena("test-arena");
        arena.setState(ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, arena.getState());
    }

    @Test
    void testArenaStateTransitionToInGame() {
        Arena arena = new Arena("test-arena");
        arena.setState(ArenaState.STARTING);
        arena.setState(ArenaState.INGAME);
        assertEquals(ArenaState.INGAME, arena.getState());
    }
}
