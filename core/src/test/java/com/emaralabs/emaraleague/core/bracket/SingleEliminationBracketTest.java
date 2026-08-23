package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SingleEliminationBracketTest {

    @Test
    void generate_twoTeams_createsOneMatch() {
        SingleEliminationBracket gen = new SingleEliminationBracket();
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);

        Bracket bracket = gen.generate(List.of(a, b));
        assertEquals(1, bracket.getTotalMatches());
        assertEquals(a, bracket.getMatches().get(0).teamA());
        assertEquals(b, bracket.getMatches().get(0).teamB());
    }

    @Test
    void generate_fourTeams_createsThreeMatches() {
        SingleEliminationBracket gen = new SingleEliminationBracket();
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);
        Team c = new Team("Gamma", 3);
        Team d = new Team("Delta", 4);

        Bracket bracket = gen.generate(List.of(a, b, c, d));
        assertEquals(3, bracket.getTotalMatches());
    }

    @Test
    void advance_firstSemiComplete_populatesFinalTeamA() {
        SingleEliminationBracket gen = new SingleEliminationBracket();
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);
        Team c = new Team("Gamma", 3);
        Team d = new Team("Delta", 4);

        Bracket bracket = gen.generate(List.of(a, b, c, d));
        Match m1 = bracket.getMatches().get(0);
        Match m1Ended = m1.withWinner(a);

        Bracket advanced = gen.advance(bracket, m1Ended);
        Match finalMatch = advanced.getMatches().get(2);
        assertEquals(a, finalMatch.teamA());
        assertNull(finalMatch.teamB());
    }

    @Test
    void advance_bothSemisComplete_populatesFinalBothTeams() {
        SingleEliminationBracket gen = new SingleEliminationBracket();
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);
        Team c = new Team("Gamma", 3);
        Team d = new Team("Delta", 4);

        Bracket bracket = gen.generate(List.of(a, b, c, d));
        Match m1 = bracket.getMatches().get(0);
        Match m2 = bracket.getMatches().get(1);

        Bracket afterM1 = gen.advance(bracket, m1.withWinner(a));
        Bracket afterM2 = gen.advance(afterM1, m2.withWinner(d));

        Match finalMatch = afterM2.getMatches().get(2);
        assertEquals(a, finalMatch.teamA());
        assertEquals(d, finalMatch.teamB());
    }

    @Test
    void advance_finalComplete_tournamentEnds() {
        SingleEliminationBracket gen = new SingleEliminationBracket();
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);

        Bracket bracket = gen.generate(List.of(a, b));
        Match finalMatch = bracket.getMatches().get(0);
        Match finalEnded = finalMatch.withWinner(a);

        Bracket advanced = gen.advance(bracket, finalEnded);
        assertEquals(1, advanced.getTotalMatches());
        assertEquals(a, advanced.getMatches().get(0).winner());
    }
}
