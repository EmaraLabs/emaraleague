package com.emaralabs.emaraleague.core.player;

import java.util.UUID;

public class PlayerSession {

    private final UUID playerId;
    private final String playerName;
    private boolean active;
    private boolean spectator;
    private UUID teamId;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private long disconnectTime;

    public PlayerSession(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.active = true;
        this.spectator = false;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.losses = 0;
        this.disconnectTime = 0;
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

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins++;
    }

    public int getLosses() {
        return losses;
    }

    public void addLoss() {
        this.losses++;
    }

    public long getDisconnectTime() {
        return disconnectTime;
    }

    public void setDisconnectTime(long disconnectTime) {
        this.disconnectTime = disconnectTime;
    }
}
