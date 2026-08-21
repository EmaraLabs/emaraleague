package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.ArrayList;
import java.util.List;

public class SingleEliminationBracket implements BracketGenerator {

    @Override
    public Bracket generate(List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        int totalMatches = teams.size() - 1;
        
        for (int i = 0; i < totalMatches; i++) {
            matches.add(new Match(null, null));
        }
        
        return new Bracket(matches);
    }

    @Override
    public Bracket advance(Bracket bracket, Match completedMatch) {
        return bracket;
    }
}
