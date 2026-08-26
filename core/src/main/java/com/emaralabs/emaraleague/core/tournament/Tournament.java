package com.emaralabs.emaraleague.core.tournament;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tournament with format-aware participant model.
 *
 * INDIVIDUAL — players compete individually, no teams created.
 * TEAM — players are grouped into teams.
 *
 * Tracks lifecycle timestamps and optional metadata.
 */
public record Tournament(
    UUID id,
    String name,
    String mode,
    BracketType bracketType,
    TournamentState state,
    List<Team> teams,
    List<Match> matches,
    Set<UUID> registeredPlayers,
    TournamentFormat format,          // NEW: INDIVIDUAL or TEAM
    int teamSize,                     // NEW: players per team (TEAM format only)
    Map<String, String> customTeamNames, // NEW: slot -> custom name
    UUID createdBy,                   // NEW: admin who created
    long createdAt,                   // NEW: timestamp
    long startedAt,                   // NEW: timestamp (0 = not started)
    long endedAt                      // NEW: timestamp (0 = not ended)
) {

    public Tournament(String name, String mode, BracketType bracketType) {
        this(UUID.randomUUID(), name, mode, bracketType, TournamentState.REGISTRATION,
                new ArrayList<>(), new ArrayList<>(), new HashSet<>(),
                TournamentFormat.TEAM, 2, new HashMap<>(), null,
                System.currentTimeMillis(), 0, 0);
    }

    public Tournament(String name, String mode, BracketType bracketType, TournamentFormat format, int teamSize) {
        this(UUID.randomUUID(), name, mode, bracketType, TournamentState.REGISTRATION,
                new ArrayList<>(), new ArrayList<>(), new HashSet<>(),
                format, teamSize, new HashMap<>(), null,
                System.currentTimeMillis(), 0, 0);
    }

    public Tournament withTeams(List<Team> teams) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public Tournament withMatches(List<Match> matches) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public Tournament withState(TournamentState newState) {
        long newStartedAt = startedAt;
        long newEndedAt = endedAt;
        if (newState == TournamentState.IN_PROGRESS && startedAt == 0) {
            newStartedAt = System.currentTimeMillis();
        }
        if (newState == TournamentState.ENDED || newState == TournamentState.CANCELLED) {
            newEndedAt = System.currentTimeMillis();
        }
        return new Tournament(id, name, mode, bracketType, newState, teams, matches, registeredPlayers,
                format, teamSize, customTeamNames, createdBy, createdAt, newStartedAt, newEndedAt);
    }

    public Tournament withCreatedBy(UUID createdBy) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public Tournament withCustomTeamNames(Map<String, String> customTeamNames) {
        return new Tournament(id, name, mode, bracketType, state, teams, matches, registeredPlayers,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public Tournament addRegisteredPlayer(UUID playerId) {
        Set<UUID> updated = new HashSet<>(registeredPlayers);
        updated.add(playerId);
        return new Tournament(id, name, mode, bracketType, state, teams, matches, updated,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public Tournament removeRegisteredPlayer(UUID playerId) {
        Set<UUID> updated = new HashSet<>(registeredPlayers);
        updated.remove(playerId);
        return new Tournament(id, name, mode, bracketType, state, teams, matches, updated,
                format, teamSize, customTeamNames, createdBy, createdAt, startedAt, endedAt);
    }

    public boolean isPlayerRegistered(UUID playerId) {
        return registeredPlayers.contains(playerId);
    }

    public int getRegisteredCount() {
        return registeredPlayers.size();
    }

    public boolean isIndividual() {
        return format == TournamentFormat.INDIVIDUAL;
    }

    public boolean isTeamBased() {
        return format == TournamentFormat.TEAM;
    }

    /**
     * Get display name for a team slot.
     * Uses custom name if configured, otherwise generates from slot.
     */
    public String getTeamDisplayName(String slot) {
        return customTeamNames.getOrDefault(slot, "Team " + slot);
    }

    /**
     * Get duration in seconds (0 if not ended).
     */
    public long getDurationSeconds() {
        if (endedAt == 0 || startedAt == 0) {
            return 0;
        }
        return (endedAt - startedAt) / 1000;
    }
}
