package com.emaralabs.emaraleague.core.arena;

import java.util.UUID;

public class Arena {

    private final UUID id;
    private final String name;
    private ArenaState state;

    public Arena(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.state = ArenaState.LOBBY;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }
}
