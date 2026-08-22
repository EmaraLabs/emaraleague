package com.emaralabs.emaraleague.core.tournament;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    @Test
    void addPlayer_addsToList() {
        Team team = new Team("Alpha", 1);
        UUID playerId = UUID.randomUUID();
        Team updated = team.addPlayer(playerId);
        assertEquals(1, updated.getPlayerCount());
        assertTrue(updated.hasPlayer(playerId));
    }

    @Test
    void addPlayer_multiplePlayers_allAdded() {
        Team team = new Team("Alpha", 1);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        Team updated = team.addPlayer(p1).addPlayer(p2);
        assertEquals(2, updated.getPlayerCount());
        assertTrue(updated.hasPlayer(p1));
        assertTrue(updated.hasPlayer(p2));
    }

    @Test
    void removePlayer_removesFromList() {
        Team team = new Team("Alpha", 1);
        UUID playerId = UUID.randomUUID();
        Team updated = team.addPlayer(playerId);
        assertEquals(1, updated.getPlayerCount());
        Team removed = updated.removePlayer(playerId);
        assertEquals(0, removed.getPlayerCount());
        assertFalse(removed.hasPlayer(playerId));
    }

    @Test
    void removePlayer_notMember_doesNothing() {
        Team team = new Team("Alpha", 1);
        UUID playerId = UUID.randomUUID();
        Team updated = team.removePlayer(playerId);
        assertEquals(0, updated.getPlayerCount());
    }

    @Test
    void getPlayerCount_empty_returnsZero() {
        Team team = new Team("Alpha", 1);
        assertEquals(0, team.getPlayerCount());
    }

    @Test
    void hasPlayer_notMember_returnsFalse() {
        Team team = new Team("Alpha", 1);
        assertFalse(team.hasPlayer(UUID.randomUUID()));
    }

    @Test
    void withPlayers_replacesList() {
        Team team = new Team("Alpha", 1);
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        Team updated = team.withPlayers(java.util.List.of(p1, p2));
        assertEquals(2, updated.getPlayerCount());
        assertTrue(updated.hasPlayer(p1));
        assertTrue(updated.hasPlayer(p2));
    }

    @Test
    void teamImmutability_originalUnchanged() {
        Team team = new Team("Alpha", 1);
        UUID playerId = UUID.randomUUID();
        team.addPlayer(playerId);
        assertEquals(0, team.getPlayerCount());
    }
}
