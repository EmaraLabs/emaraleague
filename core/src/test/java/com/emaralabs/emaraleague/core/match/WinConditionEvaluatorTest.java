package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WinConditionEvaluatorTest {

    private PlayerSessionManager sessions;
    private WinConditionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        sessions = new PlayerSessionManager();
        evaluator = new WinConditionEvaluator(sessions);
    }

    private GameMode createLastTeamStandingMode() {
        return new TestGameMode();
    }

    @Test
    void evaluate_bothAlive_returnsEmpty() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        GameMode mode = createLastTeamStandingMode();

        Optional<Team> winner = evaluator.evaluate(match, mode);
        assertTrue(winner.isEmpty());
    }

    @Test
    void evaluate_oneEliminated_returnsOtherTeam() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        TestGameMode mode = new TestGameMode();

        mode.eliminateTeam(teamA.id());

        Optional<Team> winner = evaluator.evaluate(match, mode);
        assertTrue(winner.isPresent());
        assertEquals(teamB, winner.get());
    }

    @Test
    void evaluate_otherTeamEliminated_returnsFirstTeam() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        TestGameMode mode = new TestGameMode();

        mode.eliminateTeam(teamB.id());

        Optional<Team> winner = evaluator.evaluate(match, mode);
        assertTrue(winner.isPresent());
        assertEquals(teamA, winner.get());
    }

    @Test
    void evaluate_bothEliminated_returnsEmpty() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        TestGameMode mode = new TestGameMode();

        mode.eliminateTeam(teamA.id());
        mode.eliminateTeam(teamB.id());

        Optional<Team> winner = evaluator.evaluate(match, mode);
        assertTrue(winner.isEmpty());
    }

    @Test
    void evaluate_nonLastTeamStanding_returnsEmpty() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);

        GameMode mode = new GameMode() {
            @Override public String getId() { return "other"; }
            @Override public String getDisplayName() { return "Other"; }
            @Override public int getMinPlayers() { return 2; }
            @Override public int getMaxPlayers() { return 16; }
            @Override public void onMatchStart(Match match) {}
            @Override public void onMatchTick(Match match) {}
            @Override public void onMatchEnd(Match match, Team winner) {}
            @Override public WinCondition getWinCondition() { return WinCondition.FIRST_TO_SCORE; }
        };

        Optional<Team> winner = evaluator.evaluate(match, mode);
        assertTrue(winner.isEmpty());
    }

    @Test
    void isMatchOver_oneEliminated_returnsTrue() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        TestGameMode mode = new TestGameMode();

        mode.eliminateTeam(teamA.id());

        assertTrue(evaluator.isMatchOver(match, mode));
    }

    @Test
    void isMatchOver_bothAlive_returnsFalse() {
        Team teamA = new Team("Alpha", 1);
        Team teamB = new Team("Beta", 2);
        Match match = new Match(teamA, teamB);
        TestGameMode mode = new TestGameMode();

        assertFalse(evaluator.isMatchOver(match, mode));
    }

    // ── Test Helper ─────────────────────────────────────────────────

    static class TestGameMode implements GameMode {
        private final Set<UUID> eliminatedTeams = new HashSet<>();
        private final Map<UUID, UUID> playerToTeam = new HashMap<>();

        void eliminateTeam(UUID teamId) {
            eliminatedTeams.add(teamId);
        }

        void assignPlayerToTeam(UUID playerId, UUID teamId) {
            playerToTeam.put(playerId, teamId);
        }

        @Override
        public String getId() { return "test"; }

        @Override
        public String getDisplayName() { return "Test"; }

        @Override
        public int getMinPlayers() { return 2; }

        @Override
        public int getMaxPlayers() { return 2; }

        @Override
        public void onMatchStart(Match match) {}

        @Override
        public void onMatchTick(Match match) {}

        @Override
        public void onMatchEnd(Match match, Team winner) {}

        @Override
        public WinCondition getWinCondition() { return WinCondition.LAST_TEAM_STANDING; }

        @Override
        public boolean isTeamEliminated(UUID teamId) {
            return eliminatedTeams.contains(teamId);
        }

        @Override
        public Optional<UUID> getTeamForPlayer(UUID playerId) {
            return Optional.ofNullable(playerToTeam.get(playerId));
        }
    }
}
