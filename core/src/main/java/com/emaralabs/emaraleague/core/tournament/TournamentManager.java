package com.emaralabs.emaraleague.core.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TournamentManager {

    private final Map<String, Tournament> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Tournament> byId = new ConcurrentHashMap<>();

    public Tournament createTournament(String name, String mode, BracketType bracketType) {
        if (byName.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Tournament already exists: " + name);
        }
        Tournament tournament = new Tournament(name, mode, bracketType);
        byName.put(name.toLowerCase(), tournament);
        byId.put(tournament.id(), tournament);
        return tournament;
    }

    public Optional<Tournament> getTournament(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public Optional<Tournament> getTournament(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Tournament> getTournaments() {
        return List.copyOf(byName.values());
    }

    public List<Tournament> getTournamentsByState(TournamentState state) {
        return byName.values().stream()
                .filter(t -> t.state() == state)
                .toList();
    }

    public boolean deleteTournament(String name) {
        Tournament removed = byName.remove(name.toLowerCase());
        if (removed != null) {
            byId.remove(removed.id());
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

    public Tournament transitionState(String name, TournamentState newState) {
        Tournament current = byName.get(name.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + name);
        }
        if (!canTransition(current.state(), newState)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition: %s -> %s", current.state(), newState)
            );
        }
        Tournament updated = current.withState(newState);
        byName.put(name.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        return updated;
    }

    public boolean canTransition(TournamentState current, TournamentState next) {
        return switch (current) {
            case REGISTRATION -> next == TournamentState.STARTING;
            case STARTING -> next == TournamentState.IN_PROGRESS;
            case IN_PROGRESS -> next == TournamentState.ENDED;
            case ENDED -> false;
        };
    }

    public Tournament addTeam(String tournamentName, Team team) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.state() != TournamentState.REGISTRATION) {
            throw new IllegalStateException("Cannot add teams after registration closes");
        }
        List<Team> updatedTeams = new ArrayList<>(current.teams());
        updatedTeams.add(team);
        Tournament updated = current.withTeams(updatedTeams);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        return updated;
    }

    public Tournament removeTeam(String tournamentName, UUID teamId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.state() != TournamentState.REGISTRATION) {
            throw new IllegalStateException("Cannot remove teams after registration closes");
        }
        List<Team> updatedTeams = new ArrayList<>(current.teams());
        updatedTeams.removeIf(t -> t.id().equals(teamId));
        Tournament updated = current.withTeams(updatedTeams);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        return updated;
    }

    public Optional<Team> getTeam(String tournamentName, UUID teamId) {
        return getTournament(tournamentName)
                .flatMap(t -> t.teams().stream()
                        .filter(team -> team.id().equals(teamId))
                        .findFirst());
    }

    public int getTeamCount(String tournamentName) {
        return getTournament(tournamentName)
                .map(t -> t.teams().size())
                .orElse(0);
    }
}
