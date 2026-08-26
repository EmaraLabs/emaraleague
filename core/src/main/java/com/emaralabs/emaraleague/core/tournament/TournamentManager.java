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
    private TournamentPersistence persistence;

    public void setPersistence(TournamentPersistence persistence) {
        this.persistence = persistence;
    }

    public void loadFromDatabase() {
        if (persistence == null) {
            return;
        }
        List<Tournament> loaded = persistence.findAll().join();
        for (Tournament t : loaded) {
            byName.put(t.name().toLowerCase(), t);
            byId.put(t.id(), t);
        }
    }

    /**
     * Reset tournament to clean state for fresh start.
     * Clears player registrations and resets to REGISTRATION state.
     * Called on plugin startup to fix stale state from previous session.
     */
    public void resetTournamentForStartup(String name) {
        Tournament current = byName.get(name.toLowerCase());
        if (current == null) {
            return;
        }
        // Clear all player registrations by removing each one
        Tournament cleared = current;
        for (java.util.UUID playerId : new java.util.HashSet<>(current.registeredPlayers())) {
            cleared = cleared.removeRegisteredPlayer(playerId);
        }
        // Reset to REGISTRATION state
        Tournament reset = cleared.withState(TournamentState.REGISTRATION);
        byName.put(name.toLowerCase(), reset);
        byId.put(reset.id(), reset);
        if (persistence != null) {
            persistence.update(reset);
        }
    }

    public Tournament createTournament(String name, String mode, BracketType bracketType) {
        return createTournament(name, mode, bracketType, TournamentFormat.TEAM, 2);
    }

    public Tournament createTournament(String name, String mode, BracketType bracketType, TournamentFormat format, int teamSize) {
        if (byName.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Tournament already exists: " + name);
        }
        Tournament tournament = new Tournament(name, mode, bracketType, format, teamSize);
        byName.put(name.toLowerCase(), tournament);
        byId.put(tournament.id(), tournament);
        if (persistence != null) {
            persistence.save(tournament);
        }
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
            if (persistence != null) {
                persistence.delete(removed.id());
            }
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
        if (persistence != null) {
            persistence.update(updated);
        }
        return updated;
    }

    public boolean canTransition(TournamentState current, TournamentState next) {
        return switch (current) {
            case REGISTRATION -> next == TournamentState.STARTING;
            case STARTING -> next == TournamentState.IN_PROGRESS;
            case IN_PROGRESS -> next == TournamentState.ENDED || next == TournamentState.CANCELLED;
            case ENDED -> false;
            case CANCELLED -> false;
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
        if (persistence != null) {
            persistence.update(updated);
        }
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
        if (persistence != null) {
            persistence.update(updated);
        }
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

    public Tournament assignPlayerToTeam(String tournamentName, UUID teamId, UUID playerId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.state() != TournamentState.REGISTRATION) {
            throw new IllegalStateException("Cannot assign players after registration closes");
        }

        boolean teamFound = false;
        List<Team> updatedTeams = new ArrayList<>();
        for (Team team : current.teams()) {
            if (team.id().equals(teamId)) {
                updatedTeams.add(team.addPlayer(playerId));
                teamFound = true;
            } else {
                updatedTeams.add(team);
            }
        }

        if (!teamFound) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }

        Tournament updated = current.withTeams(updatedTeams);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        if (persistence != null) {
            persistence.update(updated);
        }
        return updated;
    }

    public Tournament removePlayerFromTeam(String tournamentName, UUID teamId, UUID playerId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.state() != TournamentState.REGISTRATION) {
            throw new IllegalStateException("Cannot remove players after registration closes");
        }

        List<Team> updatedTeams = new ArrayList<>();
        for (Team team : current.teams()) {
            if (team.id().equals(teamId)) {
                updatedTeams.add(team.removePlayer(playerId));
            } else {
                updatedTeams.add(team);
            }
        }

        Tournament updated = current.withTeams(updatedTeams);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        if (persistence != null) {
            persistence.update(updated);
        }
        return updated;
    }

    public Optional<Team> getTeamForPlayer(String tournamentName, UUID playerId) {
        return getTournament(tournamentName)
                .flatMap(t -> t.teams().stream()
                        .filter(team -> team.hasPlayer(playerId))
                        .findFirst());
    }

    public List<UUID> getPlayersInTeam(String tournamentName, UUID teamId) {
        return getTournament(tournamentName)
                .flatMap(t -> t.teams().stream()
                        .filter(team -> team.id().equals(teamId))
                        .findFirst())
                .map(Team::playerIds)
                .orElse(List.of());
    }

    public int getTeamPlayerCount(String tournamentName, UUID teamId) {
        return getPlayersInTeam(tournamentName, teamId).size();
    }

    /**
     * Register player for tournament.
     * For INDIVIDUAL format: player is registered directly (no team creation).
     * For TEAM format: player is registered and auto-assigned to a team.
     */
    public Tournament registerPlayer(String tournamentName, UUID playerId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.isPlayerRegistered(playerId)) {
            throw new IllegalStateException("Player is already registered in this tournament");
        }

        // For INDIVIDUAL: just register, no team creation
        if (current.isIndividual()) {
            Tournament updated = current.addRegisteredPlayer(playerId);
            byName.put(tournamentName.toLowerCase(), updated);
            byId.put(updated.id(), updated);
            if (persistence != null) {
                persistence.update(updated);
            }
            return updated;
        }

        // For TEAM: register and auto-assign
        Tournament updated = current.addRegisteredPlayer(playerId);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        if (persistence != null) {
            persistence.update(updated);
        }

        // Auto-assign to team with fewest players
        autoAssignToTeam(tournamentName, playerId);

        return byName.get(tournamentName.toLowerCase());
    }

    public Tournament unregisterPlayer(String tournamentName, UUID playerId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        Tournament updated = current.removeRegisteredPlayer(playerId);
        byName.put(tournamentName.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        if (persistence != null) {
            persistence.update(updated);
        }
        return updated;
    }

    public boolean isPlayerRegistered(String tournamentName, UUID playerId) {
        return getTournament(tournamentName)
                .map(t -> t.isPlayerRegistered(playerId))
                .orElse(false);
    }

    public int getRegisteredCount(String tournamentName) {
        return getTournament(tournamentName)
                .map(Tournament::getRegisteredCount)
                .orElse(0);
    }

    /**
     * Auto-assign player to team with fewest players.
     * Only applicable for TEAM format tournaments.
     */
    public Tournament autoAssignToTeam(String tournamentName, UUID playerId) {
        Tournament current = byName.get(tournamentName.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + tournamentName);
        }
        if (current.state() != TournamentState.REGISTRATION) {
            throw new IllegalStateException("Cannot assign players after registration closes");
        }
        if (current.isIndividual()) {
            throw new IllegalStateException("Individual tournaments do not have teams");
        }

        // Find team with fewest players
        Team smallest = null;
        for (Team team : current.teams()) {
            if (smallest == null || team.getPlayerCount() < smallest.getPlayerCount()) {
                smallest = team;
            }
        }

        if (smallest == null) {
            throw new IllegalStateException("No teams available in tournament");
        }

        return assignPlayerToTeam(tournamentName, smallest.id(), playerId);
    }

    public Tournament cancelTournament(String name) {
        Tournament current = byName.get(name.toLowerCase());
        if (current == null) {
            throw new IllegalArgumentException("Tournament not found: " + name);
        }
        Tournament updated = current.withState(TournamentState.CANCELLED);
        byName.put(name.toLowerCase(), updated);
        byId.put(updated.id(), updated);
        if (persistence != null) {
            persistence.update(updated);
        }
        return updated;
    }

    public boolean canStart(String tournamentName) {
        return getTournament(tournamentName)
                .map(t -> {
                    if (t.isIndividual()) {
                        // INDIVIDUAL: need at least 2 players
                        return t.getRegisteredCount() >= 2;
                    } else {
                        // TEAM: need at least 2 teams with at least 1 player each
                        return t.teams().size() >= 2 &&
                                t.teams().stream().allMatch(team -> team.getPlayerCount() >= 1);
                    }
                })
                .orElse(false);
    }
}
