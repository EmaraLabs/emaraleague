package com.emaralabs.emaraleague.modules.sumo;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SumoGameModeTest {

    private SumoGameMode mode;

    @BeforeEach
    void setUp() {
        mode = new SumoGameMode();
    }

    @Test
    void testSumoGameModeId() {
        assertEquals("sumo", mode.getId());
    }

    @Test
    void testSumoGameModeDisplayName() {
        assertEquals("Sumo", mode.getDisplayName());
    }

    @Test
    void testSumoGameModeMinPlayers() {
        assertEquals(2, mode.getMinPlayers());
    }

    @Test
    void testSumoGameModeMaxPlayers() {
        assertEquals(2, mode.getMaxPlayers());
    }

    @Test
    void testWinCondition() {
        assertEquals(com.emaralabs.emaraleague.core.game.WinCondition.LAST_TEAM_STANDING, mode.getWinCondition());
    }

    // ── Knockback Tracking ──────────────────────────────────────────

    @Test
    void onKnockbackDealt_tracksAttacker() {
        UUID attackerId = UUID.randomUUID();
        Player attacker = mock(Player.class);
        when(attacker.getUniqueId()).thenReturn(attackerId);
        Player victim = mock(Player.class);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        mode.onKnockbackDealt(event);
        assertEquals(1, mode.getKnockbacksDealt(attackerId));
    }

    @Test
    void onKnockbackDealt_multipleHits_accumulates() {
        UUID attackerId = UUID.randomUUID();
        Player attacker = mock(Player.class);
        when(attacker.getUniqueId()).thenReturn(attackerId);
        Player victim = mock(Player.class);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        mode.onKnockbackDealt(event);
        mode.onKnockbackDealt(event);
        mode.onKnockbackDealt(event);
        assertEquals(3, mode.getKnockbacksDealt(attackerId));
    }

    @Test
    void getKnockbacksDealt_default_returnsZero() {
        assertEquals(0, mode.getKnockbacksDealt(UUID.randomUUID()));
    }

    @Test
    void onKnockbackDealt_nonPlayerDamager_ignores() {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        org.bukkit.entity.Zombie zombie = mock(org.bukkit.entity.Zombie.class);
        when(event.getDamager()).thenReturn(zombie);
        when(event.getEntity()).thenReturn(mock(Player.class));

        mode.onKnockbackDealt(event);
        assertEquals(0, mode.getKnockbacksDealt(UUID.randomUUID()));
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
        mode.assignPlayerToTeam(p1, teamId);
        mode.onPlayerFall(p1);
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
    void onMatchStart_clearsKnockbacks() {
        UUID attackerId = UUID.randomUUID();
        Player attacker = mock(Player.class);
        when(attacker.getUniqueId()).thenReturn(attackerId);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(mock(Player.class));
        mode.onKnockbackDealt(event);
        assertEquals(1, mode.getKnockbacksDealt(attackerId));

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        mode.onMatchStart(match);
        assertEquals(0, mode.getKnockbacksDealt(attackerId));
    }

    // ── Alive Count ─────────────────────────────────────────────────

    @Test
    void getAliveCount_bothAlive_returnsTwo() {
        assertEquals(2, mode.getAliveCount(null));
    }

    @Test
    void getAliveCount_oneEliminated_returnsOne() {
        UUID playerId = UUID.randomUUID();
        mode.onPlayerFall(playerId);
        assertEquals(1, mode.getAliveCount(null));
    }

    @Test
    void getAliveCount_bothEliminated_returnsZero() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        mode.onPlayerFall(id1);
        mode.onPlayerFall(id2);
        assertEquals(0, mode.getAliveCount(null));
    }
}
