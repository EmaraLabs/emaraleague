package com.emaralabs.emaraleague.core.arena;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaResetService {

    private final Map<UUID, Map<Location, BlockData>> trackedChanges = new HashMap<>();

    public void trackBlockBreak(Arena arena, Block block) {
        trackedChanges.computeIfAbsent(arena.getId(), k -> new HashMap<>())
                .put(block.getLocation(), block.getBlockData().clone());
    }

    public void trackBlockPlace(Arena arena, Block block) {
        trackedChanges.computeIfAbsent(arena.getId(), k -> new HashMap<>())
                .put(block.getLocation(), block.getBlockData().clone());
    }

    public void clearTrackedChanges(Arena arena) {
        trackedChanges.remove(arena.getId());
    }

    public void restoreArena(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        if (changes == null) {
            return;
        }

        for (Map.Entry<Location, BlockData> entry : changes.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            if (loc.getWorld() != null) {
                loc.getBlock().setBlockData(data);
            }
        }

        clearTrackedChanges(arena);
    }

    public boolean hasTrackedChanges(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        return changes != null && !changes.isEmpty();
    }

    public int getTrackedChangeCount(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        return changes == null ? 0 : changes.size();
    }
}
