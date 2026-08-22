package com.emaralabs.emaraleague.core.arena;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaManager {

    private final Map<String, Arena> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Arena> byId = new ConcurrentHashMap<>();

    public Arena createArena(String name) {
        if (byName.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Arena already exists: " + name);
        }
        Arena arena = new Arena(name);
        byName.put(name.toLowerCase(), arena);
        byId.put(arena.getId(), arena);
        return arena;
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public Optional<Arena> getArena(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Arena> getArenas() {
        return List.copyOf(byName.values());
    }

    public List<Arena> getArenasByState(ArenaState state) {
        return byName.values().stream()
                .filter(a -> a.getState() == state)
                .toList();
    }

    public List<Arena> getAvailableArenas() {
        return getArenasByState(ArenaState.LOBBY);
    }

    public boolean deleteArena(String name) {
        Arena removed = byName.remove(name.toLowerCase());
        if (removed != null) {
            byId.remove(removed.getId());
            return true;
        }
        return false;
    }

    public boolean exists(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    public int count() {
        return byName.size();
    }

    public Arena transitionArena(String name, ArenaState newState) {
        Arena arena = byName.get(name.toLowerCase());
        if (arena == null) {
            throw new IllegalArgumentException("Arena not found: " + name);
        }
        arena.setState(newState);
        return arena;
    }

    public boolean canTransition(String name, ArenaState newState) {
        return getArena(name)
                .map(a -> a.canTransitionTo(newState))
                .orElse(false);
    }
}
