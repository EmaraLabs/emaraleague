package com.emaralabs.emaraleague.core.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Tournament(
    UUID id,
    String name,
    String mode,
    BracketType bracketType,
    TournamentState state,
    List<Team> teams,
    List<Match> matches
) {

    public Tournament(String name, String mode, BracketType bracketType) {
        this(UUID.randomUUID(), name, mode, bracketType, TournamentState.REGISTRATION, new ArrayList<>(), new ArrayList<>());
    }

    public Tournament withTeams(List<Team> teams) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches);
    }

    public Tournament withMatches(List<Match> matches) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches);
    }

    public Tournament withState(TournamentState newState) {
        return new Tournament(id, name, mode, bracketType, newState, teams, matches);
    }
}
