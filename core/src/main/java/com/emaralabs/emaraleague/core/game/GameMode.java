package com.emaralabs.emaraleague.core.game;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.Optional;
import java.util.UUID;

public interface GameMode {

    String getId();

    String getDisplayName();

    int getMinPlayers();

    int getMaxPlayers();

    void onMatchStart(Match match);

    void onMatchTick(Match match);

    void onMatchEnd(Match match, Team winner);

    WinCondition getWinCondition();

    default boolean isTeamEliminated(UUID teamId) {
        return false;
    }

    default Optional<UUID> getTeamForPlayer(UUID playerId) {
        return Optional.empty();
    }
}
