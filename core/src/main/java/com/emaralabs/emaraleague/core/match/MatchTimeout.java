package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enforces maximum match duration.
 * P1-001 FIX: Stores EmaraTask handle and cancels it properly on match end.
 * Previously the repeating task kept running even after match ended/cancelled.
 */
public final class MatchTimeout {

    private final EmaraScheduler scheduler;
    private final MatchEngine matchEngine;
    private final Map<UUID, Long> matchStartTimes = new ConcurrentHashMap<>();
    private final Map<UUID, EmaraScheduler.EmaraTask> matchTasks = new ConcurrentHashMap<>();
    private int timeoutSeconds = 300; // 5 minutes default

    public MatchTimeout(EmaraScheduler scheduler, MatchEngine matchEngine) {
        this.scheduler = scheduler;
        this.matchEngine = matchEngine;
    }

    public void setTimeoutSeconds(int seconds) {
        this.timeoutSeconds = seconds;
    }

    public void startTimer(UUID matchId) {
        // Cancel any existing task for this match (defensive)
        cancelTimer(matchId);

        matchStartTimes.put(matchId, System.currentTimeMillis());

        EmaraScheduler.EmaraTask task = scheduler.runRepeating(() -> {
            Long startTime = matchStartTimes.get(matchId);
            if (startTime == null) {
                // Match already cleaned up — self-cancel
                cancelTimer(matchId);
                return;
            }

            Match match = matchEngine.getMatch(matchId).orElse(null);
            if (match == null || match.state() == MatchState.ENDED) {
                cancelTimer(matchId);
                return;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeoutSeconds * 1000L) {
                // Timeout — end match as draw
                endMatchOnTimeout(matchId);
                cancelTimer(matchId);
            }
        }, 20L, 20L); // Check every second

        matchTasks.put(matchId, task);
    }

    /**
     * Cancel the timer task for a match.
     * P1-001 FIX: Now actually cancels the Bukkit/Folia task, not just removes from map.
     */
    public void cancelTimer(UUID matchId) {
        matchStartTimes.remove(matchId);
        EmaraScheduler.EmaraTask task = matchTasks.remove(matchId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    /**
     * Cancel all active timers. Called on plugin disable.
     */
    public void cancelAll() {
        for (UUID matchId : matchTasks.keySet()) {
            cancelTimer(matchId);
        }
    }

    private void endMatchOnTimeout(UUID matchId) {
        Match match = matchEngine.getMatch(matchId).orElse(null);
        if (match == null) {
            return;
        }
        // End with no winner (draw)
        try {
            matchEngine.endMatch(matchId, null);
        } catch (Exception e) {
            // Match may already be ended by another path — safe to ignore
        }
    }

    public long getElapsedSeconds(UUID matchId) {
        Long start = matchStartTimes.get(matchId);
        if (start == null) {
            return 0;
        }
        return (System.currentTimeMillis() - start) / 1000;
    }

    public int getRemainingSeconds(UUID matchId) {
        return Math.max(0, timeoutSeconds - (int) getElapsedSeconds(matchId));
    }

    /**
     * Get count of active timer tasks (for diagnostics).
     */
    public int getActiveTaskCount() {
        return matchTasks.size();
    }
}
