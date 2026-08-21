package com.emaralabs.emaraleague.core.scheduler;

public interface EmaraScheduler {

    void runAsync(Runnable task);

    void runDelayed(Runnable task, long delayTicks);

    void runRepeating(Runnable task, long initialDelay, long periodTicks);
}
