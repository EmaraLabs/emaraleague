package com.emaralabs.emaraleague.core.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class PaperScheduler implements EmaraScheduler {

    private final Plugin plugin;

    public PaperScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        if (plugin != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        } else {
            task.run();
        }
    }

    @Override
    public void runDelayed(Runnable task, long delayTicks) {
        if (plugin != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    @Override
    public EmaraTask runRepeating(Runnable task, long initialDelay, long periodTicks) {
        if (plugin != null) {
            BukkitTask bukkitTask = plugin.getServer().getScheduler()
                    .runTaskTimer(plugin, task, initialDelay, periodTicks);
            return new BukkitEmaraTask(bukkitTask);
        }
        return new NoopEmaraTask();
    }

    /**
     * Wrapper around BukkitTask for cancellation.
     */
    private record BukkitEmaraTask(BukkitTask task) implements EmaraTask {
        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return task == null || task.isCancelled();
        }
    }

    /**
     * No-op task for when plugin is null (testing).
     */
    private static class NoopEmaraTask implements EmaraTask {
        @Override
        public void cancel() {}

        @Override
        public boolean isCancelled() {
            return true;
        }
    }
}
