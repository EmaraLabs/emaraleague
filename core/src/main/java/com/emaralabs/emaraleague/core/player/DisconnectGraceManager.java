package com.emaralabs.emaraleague.core.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks disconnected players and their rejoin grace periods.
 * P1-003 FIX: Uses explicit grace expiry timestamp instead of computing
 * from disconnect time on each check — eliminates timing precision issues
 * that could disqualify a player who rejoined just within the grace window.
 */
public final class DisconnectGraceManager {

    private final Map<UUID, Long> graceExpiryTimes = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerMatchMap = new ConcurrentHashMap<>();
    private int gracePeriodSeconds = 30;

    public void setGracePeriodSeconds(int seconds) {
        this.gracePeriodSeconds = seconds;
    }

    public void recordDisconnect(UUID playerId, UUID matchId) {
        // Store explicit expiry timestamp — checked with >= for full grace allowance
        graceExpiryTimes.put(playerId, System.currentTimeMillis() + (gracePeriodSeconds * 1000L));
        playerMatchMap.put(playerId, matchId);
    }

    /**
     * Check if player can still rejoin.
     * Uses >= comparison so a player rejoining at exactly grace-period
     * boundary is still allowed (no precision loss).
     */
    public boolean canRejoin(UUID playerId) {
        Long expiryTime = graceExpiryTimes.get(playerId);
        if (expiryTime == null) {
            return false;
        }
        return System.currentTimeMillis() < expiryTime;
    }

    public boolean tryRejoin(UUID playerId) {
        if (!canRejoin(playerId)) {
            return false;
        }
        graceExpiryTimes.remove(playerId);
        playerMatchMap.remove(playerId);
        return true;
    }

    public UUID getDisconnectedMatch(UUID playerId) {
        return playerMatchMap.get(playerId);
    }

    public boolean isDisconnected(UUID playerId) {
        return graceExpiryTimes.containsKey(playerId);
    }

    public long getRemainingGraceSeconds(UUID playerId) {
        Long expiryTime = graceExpiryTimes.get(playerId);
        if (expiryTime == null) {
            return 0;
        }
        long remaining = (expiryTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void clearExpired() {
        long now = System.currentTimeMillis();
        graceExpiryTimes.entrySet().removeIf(e -> now >= e.getValue());
        playerMatchMap.entrySet().removeIf(e -> !graceExpiryTimes.containsKey(e.getKey()));
    }

    public java.util.Set<UUID> getDisconnectedPlayers() {
        return new java.util.HashSet<>(graceExpiryTimes.keySet());
    }

    public void clearPlayer(UUID playerId) {
        graceExpiryTimes.remove(playerId);
        playerMatchMap.remove(playerId);
    }
}
