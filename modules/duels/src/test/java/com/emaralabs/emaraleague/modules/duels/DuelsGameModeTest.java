package com.emaralabs.emaraleague.modules.duels;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DuelsGameModeTest {

    private DuelsGameMode mode;

    @BeforeEach
    void setUp() {
        mode = new DuelsGameMode();
    }

    @Test
    void testDuelsGameModeId() {
        assertEquals("duels", mode.getId());
    }

    @Test
    void testDuelsGameModeDisplayName() {
        assertEquals("Duels", mode.getDisplayName());
    }

    @Test
    void testDuelsGameModeMinPlayers() {
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testDuelsGameModeMaxPlayers() {
        assertEquals(2, mode.getMaxPlayers());
    }

    @Test
    void testWinCondition() {
        assertEquals(com.emaralabs.emaraleague.core.game.WinCondition.LAST_TEAM_STANDING, mode.getWinCondition());
    }

    // ── Elimination ─────────────────────────────────────────────────

    @Test
    void onPlayerDeath_eliminatesPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        mode.onPlayerDeath(event);
        assertTrue(mode.isEliminated(playerId));
    }

    @Test
    void onPlayerDeath_tracksDeaths() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        mode.onPlayerDeath(event);
        assertEquals(1, mode.getDeaths(playerId));
    }

    @Test
    void onPlayerDeath_nonPlayer_ignores() {
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        org.bukkit.entity.Zombie zombie = mock(org.bukkit.entity.Zombie.class);
        when(event.getEntity()).thenReturn(zombie);

        mode.onPlayerDeath(event);
        assertEquals(2, mode.getAliveCount(null));
    }

    @Test
    void onPlayerQuit_eliminatesPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);

        mode.onPlayerQuit(event);
        assertTrue(mode.isEliminated(playerId));
    }

    @Test
    void isEliminated_notEliminated_returnsFalse() {
        assertFalse(mode.isEliminated(UUID.randomUUID()));
    }

    // ── Kill/Death Tracking ─────────────────────────────────────────

    @Test
    void getKills_default_returnsZero() {
        assertEquals(0, mode.getKills(UUID.randomUUID()));
    }

    @Test
    void getDeaths_default_returnsZero() {
        assertEquals(0, mode.getDeaths(UUID.randomUUID()));
    }

    @Test
    void getDeaths_afterDeath_returnsCount() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);

        mode.onPlayerDeath(event);
        assertEquals(1, mode.getDeaths(playerId));

        mode.onPlayerDeath(event);
        assertEquals(2, mode.getDeaths(playerId));
    }

    // ── Match Lifecycle ─────────────────────────────────────────────

    @Test
    void onMatchStart_resetsState() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);
        mode.onPlayerDeath(event);
        assertTrue(mode.isEliminated(playerId));

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchStart(match);
        assertFalse(mode.isEliminated(playerId));
        assertEquals(0, mode.getDeaths(playerId));
    }

    @Test
    void onMatchEnd_clearsState() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);
        mode.onPlayerDeath(event);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchEnd(match, new Team("Alpha", 1));
        assertFalse(mode.isEliminated(playerId));
        assertEquals(0, mode.getDeaths(playerId));
    }

    // ── Alive Count ─────────────────────────────────────────────────

    @Test
    void getAliveCount_bothAlive_returnsTwo() {
        assertEquals(2, mode.getAliveCount(null));
    }

    @Test
    void getAliveCount_oneEliminated_returnsOne() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);
        mode.onPlayerDeath(event);

        assertEquals(1, mode.getAliveCount(null));
    }

    @Test
    void getAliveCount_bothEliminated_returnsZero() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        when(p1.getUniqueId()).thenReturn(id1);
        when(p2.getUniqueId()).thenReturn(id2);

        EntityDeathEvent e1 = mock(EntityDeathEvent.class);
        when(e1.getEntity()).thenReturn(p1);
        EntityDeathEvent e2 = mock(EntityDeathEvent.class);
        when(e2.getEntity()).thenReturn(p2);

        mode.onPlayerDeath(e1);
        mode.onPlayerDeath(e2);
        assertEquals(0, mode.getAliveCount(null));
    }
}
