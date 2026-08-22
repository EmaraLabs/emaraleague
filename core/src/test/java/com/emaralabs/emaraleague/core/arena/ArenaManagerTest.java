package com.emaralabs.emaraleague.core.arena;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArenaManagerTest {

    // ── CRUD ────────────────────────────────────────────────────────

    @Test
    void createArena_returnsNewArena() {
        ArenaManager manager = new ArenaManager();
        Arena arena = manager.createArena("Arena_One");
        assertNotNull(arena);
        assertEquals("Arena_One", arena.getName());
        assertEquals(ArenaState.LOBBY, arena.getState());
        assertNotNull(arena.getId());
    }

    @Test
    void createArena_duplicateName_throwsException() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertThrows(IllegalArgumentException.class,
                () -> manager.createArena("Arena_One"));
    }

    @Test
    void createArena_duplicateNameCaseInsensitive_throwsException() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertThrows(IllegalArgumentException.class,
                () -> manager.createArena("arena_one"));
    }

    @Test
    void getArena_byName_returnsArena() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        Optional<Arena> found = manager.getArena("Arena_One");
        assertTrue(found.isPresent());
        assertEquals("Arena_One", found.get().getName());
    }

    @Test
    void getArena_byName_caseInsensitive() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertTrue(manager.getArena("arena_one").isPresent());
        assertTrue(manager.getArena("ARENA_ONE").isPresent());
    }

    @Test
    void getArena_byId_returnsArena() {
        ArenaManager manager = new ArenaManager();
        Arena created = manager.createArena("Arena_One");
        Optional<Arena> found = manager.getArena(created.getId());
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    void getArena_notFound_returnsEmpty() {
        ArenaManager manager = new ArenaManager();
        assertTrue(manager.getArena("NonExistent").isEmpty());
        assertTrue(manager.getArena(UUID.randomUUID()).isEmpty());
    }

    @Test
    void getArenas_returnsAll() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        manager.createArena("Arena_Two");
        List<Arena> all = manager.getArenas();
        assertEquals(2, all.size());
    }

    @Test
    void getArenasByState_filtersCorrectly() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        manager.createArena("Arena_Two");
        assertEquals(2, manager.getArenasByState(ArenaState.LOBBY).size());
        assertEquals(0, manager.getArenasByState(ArenaState.INGAME).size());
    }

    @Test
    void getAvailableArenas_returnsOnlyLobby() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        manager.createArena("Arena_Two");
        Arena one = manager.getArena("Arena_One").get();
        one.setState(ArenaState.STARTING);
        assertEquals(1, manager.getAvailableArenas().size());
        assertEquals("Arena_Two", manager.getAvailableArenas().get(0).getName());
    }

    @Test
    void deleteArena_removesFromBothMaps() {
        ArenaManager manager = new ArenaManager();
        Arena arena = manager.createArena("Arena_One");
        assertTrue(manager.deleteArena("Arena_One"));
        assertTrue(manager.getArena("Arena_One").isEmpty());
        assertTrue(manager.getArena(arena.getId()).isEmpty());
        assertEquals(0, manager.count());
    }

    @Test
    void deleteArena_notFound_returnsFalse() {
        ArenaManager manager = new ArenaManager();
        assertFalse(manager.deleteArena("NonExistent"));
    }

    @Test
    void exists_returnsTrueForExisting() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertTrue(manager.exists("Arena_One"));
        assertTrue(manager.exists("arena_one"));
        assertFalse(manager.exists("NonExistent"));
    }

    @Test
    void count_reflectsCurrentSize() {
        ArenaManager manager = new ArenaManager();
        assertEquals(0, manager.count());
        manager.createArena("Arena_One");
        assertEquals(1, manager.count());
        manager.createArena("Arena_Two");
        assertEquals(2, manager.count());
        manager.deleteArena("Arena_One");
        assertEquals(1, manager.count());
    }

    // ── State Delegation ────────────────────────────────────────────

    @Test
    void transitionArena_lobbyToStarting_succeeds() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        Arena updated = manager.transitionArena("Arena_One", ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, updated.getState());
    }

    @Test
    void transitionArena_fullCycle_succeeds() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        manager.transitionArena("Arena_One", ArenaState.STARTING);
        manager.transitionArena("Arena_One", ArenaState.INGAME);
        manager.transitionArena("Arena_One", ArenaState.ENDING);
        manager.transitionArena("Arena_One", ArenaState.RESETTING);
        Arena updated = manager.transitionArena("Arena_One", ArenaState.LOBBY);
        assertEquals(ArenaState.LOBBY, updated.getState());
    }

    @Test
    void transitionArena_invalidTransition_throwsException() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertThrows(IllegalStateException.class,
                () -> manager.transitionArena("Arena_One", ArenaState.INGAME));
    }

    @Test
    void transitionArena_notFound_throwsException() {
        ArenaManager manager = new ArenaManager();
        assertThrows(IllegalArgumentException.class,
                () -> manager.transitionArena("NonExistent", ArenaState.STARTING));
    }

    @Test
    void canTransition_validPaths_returnTrue() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertTrue(manager.canTransition("Arena_One", ArenaState.STARTING));
        manager.transitionArena("Arena_One", ArenaState.STARTING);
        assertTrue(manager.canTransition("Arena_One", ArenaState.INGAME));
        manager.transitionArena("Arena_One", ArenaState.INGAME);
        assertTrue(manager.canTransition("Arena_One", ArenaState.ENDING));
        manager.transitionArena("Arena_One", ArenaState.ENDING);
        assertTrue(manager.canTransition("Arena_One", ArenaState.RESETTING));
        manager.transitionArena("Arena_One", ArenaState.RESETTING);
        assertTrue(manager.canTransition("Arena_One", ArenaState.LOBBY));
    }

    @Test
    void canTransition_invalidPaths_returnFalse() {
        ArenaManager manager = new ArenaManager();
        manager.createArena("Arena_One");
        assertFalse(manager.canTransition("Arena_One", ArenaState.INGAME));
        assertFalse(manager.canTransition("Arena_One", ArenaState.ENDING));
        assertFalse(manager.canTransition("Arena_One", ArenaState.RESETTING));
    }

    @Test
    void canTransition_notFound_returnsFalse() {
        ArenaManager manager = new ArenaManager();
        assertFalse(manager.canTransition("NonExistent", ArenaState.STARTING));
    }
}
