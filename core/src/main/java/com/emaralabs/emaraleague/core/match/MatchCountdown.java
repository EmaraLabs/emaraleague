package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;

public final class MatchCountdown {

    private final EmaraScheduler scheduler;
    private final MessageRegistry messages;
    private int remainingSeconds;
    private boolean running;

    public MatchCountdown(EmaraScheduler scheduler, MessageRegistry messages) {
        this.scheduler = scheduler;
        this.messages = messages;
        this.running = false;
        this.remainingSeconds = 0;
    }

    public void startCountdown(Match match, int seconds, Runnable onComplete) {
        this.remainingSeconds = seconds;
        this.running = true;

        scheduler.runRepeating(() -> {
            if (remainingSeconds <= 0) {
                running = false;
                onComplete.run();
                return;
            }
            remainingSeconds--;
        }, 0, 20);
    }

    public void cancel() {
        running = false;
        remainingSeconds = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
