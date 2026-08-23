package com.emaralabs.emaraleague.core.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DisconnectGraceManager {

    private final Map<UUID, Long> disconnectedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerMatchMap = new ConcurrentHashMap<>();
    private int gracePeriodSeconds = 30;

    public void setGracePeriodSeconds(int seconds) {
        this.gracePeriodSeconds = seconds;
    }

    public void recordDisconnect(UUID playerId, UUID matchId) {
        disconnectedPlayers.put(playerId, System.currentTimeMillis());
        playerMatchMap.put(playerId, matchId);
    }

    public boolean canRejoin(UUID playerId) {
        Long disconnectTime = disconnectedPlayers.get(playerId);
        if (disconnectTime == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - disconnectTime;
        return elapsed < gracePeriodSeconds * 1000L;
    }

    public boolean tryRejoin(UUID playerId) {
        if (!canRejoin(playerId)) {
            return false;
        }
        disconnectedPlayers.remove(playerId);
        playerMatchMap.remove(playerId);
        return true;
    }

    public UUID getDisconnectedMatch(UUID playerId) {
        return playerMatchMap.get(playerId);
    }

    public boolean isDisconnected(UUID playerId) {
        return disconnectedPlayers.containsKey(playerId);
    }

    public long getRemainingGraceSeconds(UUID playerId) {
        Long disconnectTime = disconnectedPlayers.get(playerId);
        if (disconnectTime == null) {
            return 0;
        }
        long elapsed = (System.currentTimeMillis() - disconnectTime) / 1000;
        return Math.max(0, gracePeriodSeconds - elapsed);
    }

    public void clearExpired() {
        long now = System.currentTimeMillis();
        disconnectedPlayers.entrySet().removeIf(e -> now - e.getValue() > gracePeriodSeconds * 1000L);
        playerMatchMap.entrySet().removeIf(e -> !disconnectedPlayers.containsKey(e.getKey()));
    }
}
