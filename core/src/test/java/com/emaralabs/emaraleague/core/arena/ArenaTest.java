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
    void testValidStateTransition() {
        Arena arena = new Arena("test-arena");
        arena.setState(ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, arena.getState());
    }

    @Test
    void testInvalidStateTransition() {
        Arena arena = new Arena("test-arena");
        assertThrows(IllegalStateException.class, () -> {
            arena.setState(ArenaState.INGAME);
        });
    }

    @Test
    void testCompleteStateCycle() {
        Arena arena = new Arena("test-arena");
        
        arena.setState(ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, arena.getState());
        
        arena.setState(ArenaState.INGAME);
        assertEquals(ArenaState.INGAME, arena.getState());
        
        arena.setState(ArenaState.ENDING);
        assertEquals(ArenaState.ENDING, arena.getState());
        
        arena.setState(ArenaState.RESETTING);
        assertEquals(ArenaState.RESETTING, arena.getState());
        
        arena.setState(ArenaState.LOBBY);
        assertEquals(ArenaState.LOBBY, arena.getState());
    }

    @Test
    void testCannotSkipStates() {
        Arena arena = new Arena("test-arena");
        arena.setState(ArenaState.STARTING);
        
        assertThrows(IllegalStateException.class, () -> {
            arena.setState(ArenaState.ENDING);
        });
    }
}
