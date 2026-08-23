package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchTimeout {

    private final EmaraScheduler scheduler;
    private final MatchEngine matchEngine;
    private final Map<UUID, Long> matchStartTimes = new ConcurrentHashMap<>();
    private int timeoutSeconds = 300; // 5 minutes default

    public MatchTimeout(EmaraScheduler scheduler, MatchEngine matchEngine) {
        this.scheduler = scheduler;
        this.matchEngine = matchEngine;
    }

    public void setTimeoutSeconds(int seconds) {
        this.timeoutSeconds = seconds;
    }

    public void startTimer(UUID matchId) {
        matchStartTimes.put(matchId, System.currentTimeMillis());

        scheduler.runRepeating(() -> {
            Match match = matchEngine.getMatch(matchId).orElse(null);
            if (match == null || match.state() == MatchState.ENDED) {
                matchStartTimes.remove(matchId);
                return;
            }

            long elapsed = System.currentTimeMillis() - matchStartTimes.get(matchId);
            if (elapsed > timeoutSeconds * 1000L) {
                // Timeout — end match as draw
                endMatchOnTimeout(matchId);
                matchStartTimes.remove(matchId);
            }
        }, 20L, 20L); // Check every second
    }

    public void cancelTimer(UUID matchId) {
        matchStartTimes.remove(matchId);
    }

    private void endMatchOnTimeout(UUID matchId) {
        Match match = matchEngine.getMatch(matchId).orElse(null);
        if (match == null) {
            return;
        }
        // End with no winner (draw)
        matchEngine.endMatch(matchId, null);
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
}
