package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket;
import com.emaralabs.emaraleague.core.tournament.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MatchEngineTest {

    private TournamentManager tournaments;
    private ArenaManager arenas;
    private MatchEngine engine;

    @BeforeEach
    void setUp() {
        tournaments = new TournamentManager();
        arenas = new ArenaManager();
        engine = new MatchEngine(tournaments, arenas);
    }

    // ── Match Creation ──────────────────────────────────────────────

    @Test
    void createMatch_assignsToTournament() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team alpha = new Team("Alpha", 1);
        Team beta = new Team("Beta", 2);

        Match match = engine.createMatch("Cup", alpha, beta);
        assertNotNull(match);
        assertEquals(alpha, match.teamA());
        assertEquals(beta, match.teamB());
        assertEquals(MatchState.PENDING, match.state());
        assertNotNull(match.id());
    }

    @Test
    void createMatch_nonExistentTournament_throwsException() {
        Team alpha = new Team("Alpha", 1);
        Team beta = new Team("Beta", 2);
        assertThrows(IllegalArgumentException.class,
                () -> engine.createMatch("NonExistent", alpha, beta));
    }

    // ── Match State Machine ─────────────────────────────────────────

    @Test
    void startMatch_pendingToStarting_succeeds() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match match = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));

        Match updated = engine.startMatch(match.id());
        assertEquals(MatchState.STARTING, updated.state());
    }

    @Test
    void startMatch_startingToIngame_succeeds() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match match = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
        engine.startMatch(match.id());

        Match updated = engine.beginPlay(match.id());
        assertEquals(MatchState.INGAME, updated.state());
    }

    @Test
    void startMatch_invalidTransition_throwsException() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match match = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
        engine.startMatch(match.id());

        // Can't start an already-started match
        assertThrows(IllegalStateException.class,
                () -> engine.startMatch(match.id()));
    }

    @Test
    void startMatch_notFound_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.startMatch(UUID.randomUUID()));
    }

    @Test
    void endMatch_ingameToEnded_succeeds() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team alpha = new Team("Alpha", 1);
        Team beta = new Team("Beta", 2);
        Match match = engine.createMatch("Cup", alpha, beta);
        engine.startMatch(match.id());
        engine.beginPlay(match.id());

        Match ended = engine.endMatch(match.id(), alpha);
        assertEquals(MatchState.ENDED, ended.state());
        assertEquals(alpha, ended.winner());
    }

    @Test
    void endMatch_withWinner_recordsWinner() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team alpha = new Team("Alpha", 1);
        Team beta = new Team("Beta", 2);
        Match match = engine.createMatch("Cup", alpha, beta);
        engine.startMatch(match.id());
        engine.beginPlay(match.id());

        Match ended = engine.endMatch(match.id(), beta);
        assertEquals(beta, ended.winner());
        assertEquals(MatchState.ENDED, ended.state());
    }

    @Test
    void endMatch_notFound_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.endMatch(UUID.randomUUID(), new Team("Alpha", 1)));
    }

    // ── Match Queries ───────────────────────────────────────────────

    @Test
    void getMatch_byId_returnsMatch() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match created = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));

        Optional<Match> found = engine.getMatch(created.id());
        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
    }

    @Test
    void getMatch_notFound_returnsEmpty() {
        assertTrue(engine.getMatch(UUID.randomUUID()).isEmpty());
    }

    @Test
    void getMatches_returnsAllForTournament() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
        engine.createMatch("Cup", new Team("Gamma", 3), new Team("Delta", 4));

        List<Match> matches = engine.getMatches("Cup");
        assertEquals(2, matches.size());
    }

    @Test
    void getMatches_nonExistentTournament_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.getMatches("NonExistent"));
    }

    @Test
    void getMatchesByState_filtersCorrectly() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match m1 = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
        Match m2 = engine.createMatch("Cup", new Team("Gamma", 3), new Team("Delta", 4));
        engine.startMatch(m1.id());

        assertEquals(1, engine.getMatchesByState("Cup", MatchState.STARTING).size());
        assertEquals(1, engine.getMatchesByState("Cup", MatchState.PENDING).size());
        assertEquals(0, engine.getMatchesByState("Cup", MatchState.ENDED).size());
    }

    @Test
    void getActiveMatchCount_countsOnlyActive() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Match m1 = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
        Match m2 = engine.createMatch("Cup", new Team("Gamma", 3), new Team("Delta", 4));

        assertEquals(0, engine.getActiveMatchCount("Cup"));

        engine.startMatch(m1.id());
        assertEquals(1, engine.getActiveMatchCount("Cup"));

        engine.beginPlay(m1.id());
        engine.endMatch(m1.id(), new Team("Alpha", 1));
        assertEquals(0, engine.getActiveMatchCount("Cup"));
    }

    // ── Bracket Integration ─────────────────────────────────────────

    @Test
    void generateBracket_createsMatchesFromTeams() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        tournaments.addTeam("Cup", new Team("Alpha", 1));
        tournaments.addTeam("Cup", new Team("Beta", 2));

        Bracket bracket = engine.generateBracket("Cup", new SingleEliminationBracket());
        assertNotNull(bracket);
        assertTrue(bracket.getTotalMatches() > 0);
    }

    @Test
    void generateBracket_lessThanTwoTeams_throwsException() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        tournaments.addTeam("Cup", new Team("Alpha", 1));

        assertThrows(IllegalStateException.class,
                () -> engine.generateBracket("Cup", new SingleEliminationBracket()));
    }

    @Test
    void generateBracket_nonExistentTournament_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.generateBracket("NonExistent", new SingleEliminationBracket()));
    }

    @Test
    void getNextMatch_returnsFirstPending() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        tournaments.addTeam("Cup", new Team("Alpha", 1));
        tournaments.addTeam("Cup", new Team("Beta", 2));
        engine.generateBracket("Cup", new SingleEliminationBracket());

        Optional<Match> next = engine.getNextMatch("Cup");
        assertTrue(next.isPresent());
        assertEquals(MatchState.PENDING, next.get().state());
    }

    @Test
    void getNextMatch_noPending_returnsEmpty() {
        tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        tournaments.addTeam("Cup", new Team("Alpha", 1));
        tournaments.addTeam("Cup", new Team("Beta", 2));
        engine.generateBracket("Cup", new SingleEliminationBracket());

        // Start and end all matches
        for (Match m : engine.getMatches("Cup")) {
            engine.startMatch(m.id());
            engine.beginPlay(m.id());
            engine.endMatch(m.id(), m.teamA());
        }

        assertTrue(engine.getNextMatch("Cup").isEmpty());
    }
}
