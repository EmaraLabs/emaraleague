package com.emaralabs.emaraleague.core.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStats {

    private final Map<UUID, Integer> wins = new HashMap<>();
    private final Map<UUID, Integer> losses = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();

    public void addWin(UUID playerId) {
        wins.merge(playerId, 1, Integer::sum);
    }

    public void addLoss(UUID playerId) {
        losses.merge(playerId, 1, Integer::sum);
    }

    public void addKill(UUID playerId) {
        kills.merge(playerId, 1, Integer::sum);
    }

    public void addDeath(UUID playerId) {
        deaths.merge(playerId, 1, Integer::sum);
    }

    public int getWins(UUID playerId) {
        return wins.getOrDefault(playerId, 0);
    }

    public int getLosses(UUID playerId) {
        return losses.getOrDefault(playerId, 0);
    }

    public int getKills(UUID playerId) {
        return kills.getOrDefault(playerId, 0);
    }

    public int getDeaths(UUID playerId) {
        return deaths.getOrDefault(playerId, 0);
    }

    public double getWinRate(UUID playerId) {
        int w = getWins(playerId);
        int l = getLosses(playerId);
        int total = w + l;
        return total == 0 ? 0.0 : (double) w / total;
    }

    public double getKDRatio(UUID playerId) {
        int k = getKills(playerId);
        int d = getDeaths(playerId);
        return d == 0 ? k : (double) k / d;
    }

    public void reset(UUID playerId) {
        wins.remove(playerId);
        losses.remove(playerId);
        kills.remove(playerId);
        deaths.remove(playerId);
    }
}
