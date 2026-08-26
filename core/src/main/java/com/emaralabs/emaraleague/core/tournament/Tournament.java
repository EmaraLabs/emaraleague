package com.emaralabs.emaraleague.core.tournament;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Tournament(
    UUID id,
    String name,
    String mode,
    BracketType bracketType,
    TournamentState state,
    List<Team> teams,
    List<Match> matches,
    Set<UUID> registeredPlayers
) {

    public Tournament(String name, String mode, BracketType bracketType) {
        this(UUID.randomUUID(), name, mode, bracketType, TournamentState.REGISTRATION,
                new ArrayList<>(), new ArrayList<>(), new HashSet<>());
    }

    public Tournament withTeams(List<Team> teams) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers);
    }

    public Tournament withMatches(List<Match> matches) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers);
    }

    public Tournament withState(TournamentState newState) {
        return new Tournament(id, name, mode, bracketType, newState, teams, matches, registeredPlayers);
    }

    public Tournament addRegisteredPlayer(UUID playerId) {
        Set<UUID> updated = new HashSet<>(registeredPlayers);
        updated.add(playerId);
        return new Tournament(id, name, mode, bracketType, state, teams, matches, updated);
    }

    public Tournament removeRegisteredPlayer(UUID playerId) {
        Set<UUID> updated = new HashSet<>(registeredPlayers);
        updated.remove(playerId);
        return new Tournament(id, name, mode, bracketType, state, teams, matches, updated);
    }

    public boolean isPlayerRegistered(UUID playerId) {
        return registeredPlayers.contains(playerId);
    }

    public int getRegisteredCount() {
        return registeredPlayers.size();
    }
}
