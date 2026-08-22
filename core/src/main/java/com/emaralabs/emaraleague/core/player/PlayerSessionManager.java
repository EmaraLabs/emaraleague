package com.emaralabs.emaraleague.core.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSessionManager {

    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();

    public PlayerSession createSession(UUID playerId, String playerName) {
        PlayerSession session = new PlayerSession(playerId, playerName);
        sessions.put(playerId, session);
        return session;
    }

    public Optional<PlayerSession> getSession(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public void removeSession(UUID playerId) {
        sessions.remove(playerId);
    }

    public void assignToTeam(UUID playerId, UUID teamId) {
        getSession(playerId).ifPresent(s -> s.setTeamId(teamId));
    }

    public void setSpectator(UUID playerId, boolean spectator) {
        getSession(playerId).ifPresent(s -> s.setSpectator(spectator));
    }

    public Optional<UUID> getTeamId(UUID playerId) {
        return getSession(playerId).map(PlayerSession::getTeamId);
    }

    public boolean isInMatch(UUID playerId) {
        return getSession(playerId).map(s -> s.getTeamId() != null).orElse(false);
    }

    public void clearMatch(UUID playerId) {
        getSession(playerId).ifPresent(s -> s.setTeamId(null));
    }
}
