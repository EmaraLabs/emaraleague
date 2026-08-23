package com.emaralabs.emaraleague.core.player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SpectatorManager {

    private final Map<UUID, UUID> spectatorToMatch = new HashMap<>();
    private final Map<UUID, UUID> spectatorToArena = new HashMap<>();

    public void addSpectator(UUID playerId, UUID matchId, UUID arenaId) {
        spectatorToMatch.put(playerId, matchId);
        spectatorToArena.put(playerId, arenaId);
    }

    public void removeSpectator(UUID playerId) {
        spectatorToMatch.remove(playerId);
        spectatorToArena.remove(playerId);
    }

    public boolean isSpectator(UUID playerId) {
        return spectatorToMatch.containsKey(playerId);
    }

    public Optional<UUID> getSpectatedMatch(UUID playerId) {
        return Optional.ofNullable(spectatorToMatch.get(playerId));
    }

    public Optional<UUID> getSpectatedArena(UUID playerId) {
        return Optional.ofNullable(spectatorToArena.get(playerId));
    }

    public int getSpectatorCount(UUID matchId) {
        return (int) spectatorToMatch.values().stream()
                .filter(id -> id.equals(matchId))
                .count();
    }

    public void clearMatchSpectators(UUID matchId) {
        spectatorToMatch.entrySet().removeIf(e -> e.getValue().equals(matchId));
        spectatorToArena.entrySet().removeIf(e -> {
            UUID arenaId = spectatorToArena.get(e.getKey());
            return arenaId != null && arenaId.equals(spectatorToArena.get(e.getKey()));
        });
    }
}
