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

    public void setState(ArenaState newState) {
        validateStateTransition(this.state, newState);
        this.state = newState;
    }

    private void validateStateTransition(ArenaState current, ArenaState next) {
        boolean valid = switch (current) {
            case LOBBY -> next == ArenaState.STARTING;
            case STARTING -> next == ArenaState.INGAME;
            case INGAME -> next == ArenaState.ENDING;
            case ENDING -> next == ArenaState.RESETTING;
            case RESETTING -> next == ArenaState.LOBBY;
        };
        
        if (!valid) {
            throw new IllegalStateException(
                String.format("Invalid state transition: %s -> %s", current, next)
            );
        }
    }
}
