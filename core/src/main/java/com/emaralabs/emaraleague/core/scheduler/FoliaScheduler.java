package com.emaralabs.emaraleague.core.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler implements EmaraScheduler {

    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        if (plugin != null) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            task.run();
        }
    }

    @Override
    public void runDelayed(Runnable task, long delayTicks) {
        if (plugin != null) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        }
    }

    @Override
    public void runRepeating(Runnable task, long initialDelay, long periodTicks) {
        if (plugin != null) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), initialDelay, periodTicks);
        }
    }

    public void runOnRegion(Runnable task, Location location) {
        if (plugin != null) {
            plugin.getServer().getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        }
    }

    public void runOnEntity(Runnable task, Entity entity) {
        if (plugin != null) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        }
    }
}
