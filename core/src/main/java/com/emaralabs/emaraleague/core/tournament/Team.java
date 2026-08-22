package com.emaralabs.emaraleague.core.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Team(UUID id, String name, int seed, List<UUID> playerIds) {

    public Team(String name, int seed) {
        this(UUID.randomUUID(), name, seed, new ArrayList<>());
    }

    public Team withPlayers(List<UUID> playerIds) {
        return new Team(id, name, seed, playerIds);
    }

    public Team addPlayer(UUID playerId) {
        List<UUID> updated = new ArrayList<>(playerIds);
        updated.add(playerId);
        return new Team(id, name, seed, updated);
    }

    public Team removePlayer(UUID playerId) {
        List<UUID> updated = new ArrayList<>(playerIds);
        updated.remove(playerId);
        return new Team(id, name, seed, updated);
    }

    public int getPlayerCount() {
        return playerIds.size();
    }

    public boolean hasPlayer(UUID playerId) {
        return playerIds.contains(playerId);
    }
}
