package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Team;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BracketGeneratorTest {

    @Test
    void testSingleEliminationWith4Teams() {
        SingleEliminationBracket bracket = new SingleEliminationBracket();
        List<Team> teams = List.of(
            new Team("Team1", 1),
            new Team("Team2", 2),
            new Team("Team3", 3),
            new Team("Team4", 4)
        );
        Bracket result = bracket.generate(teams);
        assertNotNull(result);
        assertEquals(3, result.getTotalMatches());
    }

    @Test
    void testSingleEliminationWith8Teams() {
        SingleEliminationBracket bracket = new SingleEliminationBracket();
        List<Team> teams = List.of(
            new Team("Team1", 1),
            new Team("Team2", 2),
            new Team("Team3", 3),
            new Team("Team4", 4),
            new Team("Team5", 5),
            new Team("Team6", 6),
            new Team("Team7", 7),
            new Team("Team8", 8)
        );
        Bracket result = bracket.generate(teams);
        assertNotNull(result);
        assertEquals(7, result.getTotalMatches());
    }

    @Test
    void testRoundRobinWith4Teams() {
        RoundRobinBracket bracket = new RoundRobinBracket();
        List<Team> teams = List.of(
            new Team("Team1", 1),
            new Team("Team2", 2),
            new Team("Team3", 3),
            new Team("Team4", 4)
        );
        Bracket result = bracket.generate(teams);
        assertNotNull(result);
        assertEquals(6, result.getTotalMatches());
    }
}
