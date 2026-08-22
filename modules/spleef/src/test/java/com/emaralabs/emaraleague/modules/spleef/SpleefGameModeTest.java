package com.emaralabs.emaraleague.modules.spleef;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpleefGameModeTest {

    private SpleefGameMode mode;

    @BeforeEach
    void setUp() {
        mode = new SpleefGameMode();
    }

    @Test
    void testSpleefGameModeId() {
        assertEquals("spleef", mode.getId());
    }

    @Test
    void testSpleefGameModeDisplayName() {
        assertEquals("Spleef", mode.getDisplayName());
    }

    @Test
    void testSpleefGameModeMinPlayers() {
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testSpleefGameModeMaxPlayers() {
        assertEquals(16, mode.getMaxPlayers());
    }

    @Test
    void testWinCondition() {
        assertEquals(com.emaralabs.emaraleague.core.game.WinCondition.LAST_TEAM_STANDING, mode.getWinCondition());
    }

    // ── Block Break Tracking ────────────────────────────────────────

    @Test
    void onBlockBreak_tracksPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);

        mode.onBlockBreak(event);
        assertEquals(1, mode.getBlocksBroken(playerId));
    }

    @Test
    void onBlockBreak_multipleBreaks_accumulates() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);

        mode.onBlockBreak(event);
        mode.onBlockBreak(event);
        mode.onBlockBreak(event);
        assertEquals(3, mode.getBlocksBroken(playerId));
    }

    @Test
    void getBlocksBroken_default_returnsZero() {
        assertEquals(0, mode.getBlocksBroken(UUID.randomUUID()));
    }

    // ── Elimination ─────────────────────────────────────────────────

    @Test
    void onPlayerFall_eliminatesPlayer() {
        UUID playerId = UUID.randomUUID();
        mode.onPlayerFall(playerId);
        assertTrue(mode.isEliminated(playerId));
    }

    @Test
    void isEliminated_notEliminated_returnsFalse() {
        assertFalse(mode.isEliminated(UUID.randomUUID()));
    }

    // ── Team Support ────────────────────────────────────────────────

    @Test
    void assignPlayerToTeam_storesMapping() {
        UUID playerId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        mode.assignPlayerToTeam(playerId, teamId);
        assertEquals(Optional.of(teamId), mode.getTeamForPlayer(playerId));
    }

    @Test
    void getTeamForPlayer_notAssigned_returnsEmpty() {
        assertTrue(mode.getTeamForPlayer(UUID.randomUUID()).isEmpty());
    }

    @Test
    void isTeamEliminated_allPlayersEliminated_returnsTrue() {
        UUID teamId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        mode.assignPlayerToTeam(p1, teamId);
        mode.assignPlayerToTeam(p2, teamId);
        mode.onPlayerFall(p1);
        mode.onPlayerFall(p2);
        assertTrue(mode.isTeamEliminated(teamId));
    }

    @Test
    void isTeamEliminated_someAlive_returnsFalse() {
        UUID teamId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        mode.assignPlayerToTeam(p1, teamId);
        mode.assignPlayerToTeam(p2, teamId);
        mode.onPlayerFall(p1);
        assertFalse(mode.isTeamEliminated(teamId));
    }

    @Test
    void isTeamEliminated_noPlayers_returnsTrue() {
        assertTrue(mode.isTeamEliminated(UUID.randomUUID()));
    }

    // ── Match Lifecycle ─────────────────────────────────────────────

    @Test
    void onMatchStart_resetsState() {
        UUID playerId = UUID.randomUUID();
        mode.onPlayerFall(playerId);
        assertTrue(mode.isEliminated(playerId));

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchStart(match);
        assertFalse(mode.isEliminated(playerId));
    }

    @Test
    void onMatchEnd_clearsState() {
        UUID playerId = UUID.randomUUID();
        mode.onPlayerFall(playerId);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchEnd(match, new Team("Alpha", 1));
        assertFalse(mode.isEliminated(playerId));
    }

    @Test
    void onMatchStart_clearsBlocksBroken() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);

        BlockBreakEvent event = mock(BlockBreakEvent.class);
        when(event.getPlayer()).thenReturn(player);
        mode.onBlockBreak(event);
        assertEquals(1, mode.getBlocksBroken(playerId));

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchStart(match);
        assertEquals(0, mode.getBlocksBroken(playerId));
    }
}
