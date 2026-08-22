package com.emaralabs.emaraleague.core.integration;

import com.emaralabs.emaraleague.core.arena.Arena;
import com.emaralabs.emaraleague.core.arena.ArenaState;
import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket;
import com.emaralabs.emaraleague.core.tournament.BracketType;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import com.emaralabs.emaraleague.core.tournament.Tournament;
import com.emaralabs.emaraleague.core.tournament.TournamentState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentFlowIntegrationTest {

    @Test
    void testCompleteTournamentFlow() {
        Tournament tournament = new Tournament("Summer Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertEquals(TournamentState.REGISTRATION, tournament.state());

        Team team1 = new Team("Dragons", 1);
        Team team2 = new Team("Tigers", 2);
        Team team3 = new Team("Wolves", 3);
        Team team4 = new Team("Eagles", 4);

        tournament = tournament.withTeams(List.of(team1, team2, team3, team4));
        assertEquals(4, tournament.teams().size());

        SingleEliminationBracket bracketGen = new SingleEliminationBracket();
        Bracket bracket = bracketGen.generate(tournament.teams());
        assertEquals(3, bracket.getTotalMatches());

        Match finalMatch = bracket.getMatches().get(0);
        assertNotNull(finalMatch.teamA());
        assertNotNull(finalMatch.teamB());

        tournament = tournament.withMatches(bracket.getMatches());
        tournament = tournament.withState(TournamentState.IN_PROGRESS);
        assertEquals(TournamentState.IN_PROGRESS, tournament.state());
    }

    @Test
    void testArenaIntegration() {
        Arena arena = new Arena("main-arena");
        assertEquals(ArenaState.LOBBY, arena.getState());

        arena.setState(ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, arena.getState());

        arena.setState(ArenaState.INGAME);
        assertEquals(ArenaState.INGAME, arena.getState());
    }

    @Test
    void testBracketWithRealTeams() {
        SingleEliminationBracket bracketGen = new SingleEliminationBracket();
        
        Team team1 = new Team("Team Alpha", 1);
        Team team2 = new Team("Team Beta", 2);
        
        Bracket bracket = bracketGen.generate(List.of(team1, team2));
        
        assertEquals(1, bracket.getTotalMatches());
        Match match = bracket.getMatches().get(0);
        assertEquals("Team Alpha", match.teamA().name());
        assertEquals("Team Beta", match.teamB().name());
    }
}
