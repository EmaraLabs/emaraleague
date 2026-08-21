package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.ArrayList;
import java.util.List;

public class SingleEliminationBracket implements BracketGenerator {

    @Override
    public Bracket generate(List<Team> teams) {
        if (teams == null || teams.size() < 2) {
            throw new IllegalArgumentException("At least 2 teams required");
        }

        List<Team> shuffled = new ArrayList<>(teams);
        List<Match> matches = new ArrayList<>();
        
        int matchCount = teams.size() - 1;
        for (int i = 0; i < matchCount; i++) {
            matches.add(new Match(null, null));
        }
        
        int firstRoundMatches = getFirstRoundMatchCount(teams.size());
        for (int i = 0; i < firstRoundMatches; i++) {
            int teamAIndex = i * 2;
            int teamBIndex = i * 2 + 1;
            
            Team teamA = teamAIndex < shuffled.size() ? shuffled.get(teamAIndex) : null;
            Team teamB = teamBIndex < shuffled.size() ? shuffled.get(teamBIndex) : null;
            
            if (teamA != null && teamB != null) {
                matches.set(i, new Match(teamA, teamB));
            }
        }
        
        return new Bracket(matches);
    }

    @Override
    public Bracket advance(Bracket bracket, Match completedMatch) {
        return bracket;
    }

    private int getFirstRoundMatchCount(int teamCount) {
        int powerOfTwo = 1;
        while (powerOfTwo * 2 <= teamCount) {
            powerOfTwo *= 2;
        }
        return powerOfTwo / 2;
    }
}
