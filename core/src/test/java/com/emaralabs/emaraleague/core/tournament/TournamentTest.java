package com.emaralabs.emaraleague.core.tournament;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TournamentTest {

    @Test
    void testTournamentCreation() {
        Tournament tournament = new Tournament("Summer Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertEquals("Summer Cup", tournament.name());
        assertEquals("duels", tournament.mode());
        assertEquals(BracketType.SINGLE_ELIMINATION, tournament.bracketType());
        assertEquals(TournamentState.REGISTRATION, tournament.state());
    }

    @Test
    void testTeamCreation() {
        Team team = new Team("Dragons", 1);
        assertEquals("Dragons", team.name());
        assertEquals(1, team.seed());
    }

    @Test
    void testMatchCreation() {
        Team teamA = new Team("Team A", 1);
        Team teamB = new Team("Team B", 2);
        Match match = new Match(teamA, teamB);
        assertEquals(teamA, match.teamA());
        assertEquals(teamB, match.teamB());
        assertEquals(MatchState.PENDING, match.state());
    }
}
