package com.emaralabs.emaraleague.core.player;

import java.util.UUID;

public class PlayerSession {

    private final UUID playerId;
    private final String playerName;
    private boolean active;
    private boolean spectator;

    public PlayerSession(String playerName) {
        this.playerId = UUID.randomUUID();
        this.playerName = playerName;
        this.active = true;
        this.spectator = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }
}
