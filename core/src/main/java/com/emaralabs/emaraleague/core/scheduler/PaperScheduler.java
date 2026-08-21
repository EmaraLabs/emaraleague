package com.emaralabs.emaraleague.core.scheduler;

import org.bukkit.plugin.Plugin;

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
    public void runRepeating(Runnable task, long initialDelay, long periodTicks) {
        if (plugin != null) {
            plugin.getServer().getScheduler().runTaskTimer(plugin, task, initialDelay, periodTicks);
        }
    }
}
