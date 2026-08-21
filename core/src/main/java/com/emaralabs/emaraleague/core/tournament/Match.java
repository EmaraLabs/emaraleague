package com.emaralabs.emaraleague.core.tournament;

import java.util.UUID;

public record Match(UUID id, Team teamA, Team teamB, MatchState state, Team winner) {

    public Match(Team teamA, Team teamB) {
        this(UUID.randomUUID(), teamA, teamB, MatchState.PENDING, null);
    }

    public Match withState(MatchState newState) {
        return new Match(id, teamA, teamB, newState, winner);
    }

    public Match withWinner(Team winner) {
        return new Match(id, teamA, teamB, MatchState.ENDED, winner);
    }
}
