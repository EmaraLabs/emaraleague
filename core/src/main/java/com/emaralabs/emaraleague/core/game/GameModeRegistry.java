package com.emaralabs.emaraleague.core.game;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GameModeRegistry {

    private final Map<String, GameMode> modes = new ConcurrentHashMap<>();

    public void register(GameMode mode) {
        modes.put(mode.getId().toLowerCase(), mode);
    }

    public void unregister(String id) {
        modes.remove(id.toLowerCase());
    }

    public Optional<GameMode> getMode(String id) {
        return Optional.ofNullable(modes.get(id.toLowerCase()));
    }

    public List<GameMode> getModes() {
        return List.copyOf(modes.values());
    }

    public List<String> getModeIds() {
        return List.copyOf(modes.keySet());
    }

    public boolean exists(String id) {
        return modes.containsKey(id.toLowerCase());
    }

    public int count() {
        return modes.size();
    }
}
