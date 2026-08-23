package com.emaralabs.emaraleague.modules.duels;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DuelsGameModeTest {

    private DuelsGameMode mode;
    private Match match;

    @BeforeEach
    void setUp() {
        mode = new DuelsGameMode();
        match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchStart(match);
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

    @Test
    void onMatchStart_initializesState() {
        assertEquals(2, mode.getAliveCount(match));
    }

    @Test
    void onPlayerDeath_eliminatesPlayer() {
        UUID playerId = UUID.randomUUID();
        mode.markEliminated(playerId);
        assertTrue(mode.isEliminated(playerId));
    }

    @Test
    void onPlayerDeath_tracksDeaths() {
        UUID playerId = UUID.randomUUID();
        mode.markEliminated(playerId);
        assertEquals(1, mode.getDeaths(playerId));
    }

    @Test
    void getKills_default_returnsZero() {
        assertEquals(0, mode.getKills(UUID.randomUUID()));
    }

    @Test
    void getAliveCount_oneEliminated_returnsOne() {
        mode.markEliminated(UUID.randomUUID());
        assertEquals(1, mode.getAliveCount(match));
    }

    @Test
    void getAliveCount_bothEliminated_returnsZero() {
        mode.markEliminated(UUID.randomUUID());
        mode.markEliminated(UUID.randomUUID());
        assertEquals(0, mode.getAliveCount(match));
    }

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
        mode.assignPlayerToTeam(p1, teamId);
        mode.markEliminated(p1);
        assertTrue(mode.isTeamEliminated(teamId));
    }

    @Test
    void isTeamEliminated_someAlive_returnsFalse() {
        UUID teamId = UUID.randomUUID();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        mode.assignPlayerToTeam(p1, teamId);
        mode.assignPlayerToTeam(p2, teamId);
        mode.markEliminated(p1);
        assertFalse(mode.isTeamEliminated(teamId));
    }

    @Test
    void onMatchEnd_clearsState() {
        UUID playerId = UUID.randomUUID();
        mode.markEliminated(playerId);
        mode.onMatchEnd(match, new Team("Alpha", 1));
        assertFalse(mode.isEliminated(playerId));
    }

    @Test
    void onPlayerDeathEvent_marksEliminated() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        EntityDeathEvent event = mock(EntityDeathEvent.class);
        when(event.getEntity()).thenReturn(player);
        mode.onPlayerDeath(event);
        assertTrue(mode.isEliminated(playerId));
    }

    @Test
    void onPlayerQuitEvent_marksEliminated() {
        UUID playerId = UUID.randomUUID();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(playerId);
        PlayerQuitEvent event = mock(PlayerQuitEvent.class);
        when(event.getPlayer()).thenReturn(player);
        mode.onPlayerQuit(event);
        assertTrue(mode.isEliminated(playerId));
    }
}
