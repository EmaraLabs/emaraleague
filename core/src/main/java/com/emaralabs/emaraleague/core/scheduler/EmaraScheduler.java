package com.emaralabs.emaraleague.core.scheduler;

/**
 * Platform-agnostic scheduler abstraction.
 * All repeating/delayed tasks return a handle that can be cancelled.
 * P1-001 FIX: runRepeating now returns EmaraTask for proper cancellation.
 */
public interface EmaraScheduler {

    void runAsync(Runnable task);

    void runDelayed(Runnable task, long delayTicks);

    /**
     * Run a repeating task.
     * @return EmaraTask handle that MUST be cancelled when no longer needed
     */
    EmaraTask runRepeating(Runnable task, long initialDelay, long periodTicks);

    /**
     * Handle to a scheduled task that can be cancelled.
     */
    interface EmaraTask {
        void cancel();
        boolean isCancelled();
    }
}
