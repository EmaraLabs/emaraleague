package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.Optional;

public final class WinConditionEvaluator {

    private final PlayerSessionManager sessions;

    public WinConditionEvaluator(PlayerSessionManager sessions) {
        this.sessions = sessions;
    }

    public Optional<Team> evaluate(Match match, GameMode mode) {
        if (mode.getWinCondition() != WinCondition.LAST_TEAM_STANDING) {
            return Optional.empty();
        }

        Team teamA = match.teamA();
        Team teamB = match.teamB();

        boolean teamAAlive = !mode.isTeamEliminated(teamA.id());
        boolean teamBAlive = !mode.isTeamEliminated(teamB.id());

        if (teamAAlive && !teamBAlive) {
            return Optional.of(teamA);
        }
        if (!teamAAlive && teamBAlive) {
            return Optional.of(teamB);
        }
        return Optional.empty();
    }

    public boolean isMatchOver(Match match, GameMode mode) {
        return evaluate(match, mode).isPresent();
    }
}
