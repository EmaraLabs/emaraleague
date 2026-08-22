package com.emaralabs.emaraleague.core.game;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameModeRegistryTest {

    private GameModeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new GameModeRegistry();
    }

    private GameMode createMockMode(String id, String displayName) {
        return new GameMode() {
            @Override public String getId() { return id; }
            @Override public String getDisplayName() { return displayName; }
            @Override public int getMinPlayers() { return 2; }
            @Override public int getMaxPlayers() { return 16; }
            @Override public void onMatchStart(Match match) {}
            @Override public void onMatchTick(Match match) {}
            @Override public void onMatchEnd(Match match, Team winner) {}
            @Override public WinCondition getWinCondition() { return WinCondition.LAST_TEAM_STANDING; }
        };
    }

    @Test
    void register_addsMode() {
        GameMode mode = createMockMode("duels", "Duels");
        registry.register(mode);
        assertTrue(registry.exists("duels"));
        assertEquals(1, registry.count());
    }

    @Test
    void getMode_byId_returnsMode() {
        GameMode mode = createMockMode("duels", "Duels");
        registry.register(mode);
        Optional<GameMode> found = registry.getMode("duels");
        assertTrue(found.isPresent());
        assertEquals("Duels", found.get().getDisplayName());
    }

    @Test
    void getMode_caseInsensitive() {
        GameMode mode = createMockMode("Duels", "Duels");
        registry.register(mode);
        assertTrue(registry.getMode("duels").isPresent());
        assertTrue(registry.getMode("DUELS").isPresent());
        assertTrue(registry.getMode("Duels").isPresent());
    }

    @Test
    void getMode_notFound_returnsEmpty() {
        assertTrue(registry.getMode("nonexistent").isEmpty());
    }

    @Test
    void getModes_returnsAll() {
        registry.register(createMockMode("duels", "Duels"));
        registry.register(createMockMode("spleef", "Spleef"));
        List<GameMode> all = registry.getModes();
        assertEquals(2, all.size());
    }

    @Test
    void getModeIds_returnsAllIds() {
        registry.register(createMockMode("duels", "Duels"));
        registry.register(createMockMode("spleef", "Spleef"));
        List<String> ids = registry.getModeIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("duels"));
        assertTrue(ids.contains("spleef"));
    }

    @Test
    void exists_returnsTrueForExisting() {
        registry.register(createMockMode("duels", "Duels"));
        assertTrue(registry.exists("duels"));
        assertTrue(registry.exists("DUELS"));
        assertFalse(registry.exists("nonexistent"));
    }

    @Test
    void count_reflectsCurrentSize() {
        assertEquals(0, registry.count());
        registry.register(createMockMode("duels", "Duels"));
        assertEquals(1, registry.count());
        registry.register(createMockMode("spleef", "Spleef"));
        assertEquals(2, registry.count());
    }

    @Test
    void register_duplicate_overwrites() {
        GameMode mode1 = createMockMode("duels", "Duels v1");
        GameMode mode2 = createMockMode("duels", "Duels v2");
        registry.register(mode1);
        registry.register(mode2);
        assertEquals(1, registry.count());
        assertEquals("Duels v2", registry.getMode("duels").get().getDisplayName());
    }
}
