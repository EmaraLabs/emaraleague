package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.ArrayList;
import java.util.List;

public class RoundRobinBracket implements BracketGenerator {

    @Override
    public Bracket generate(List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                matches.add(new Match(teams.get(i), teams.get(j)));
            }
        }
        
        return new Bracket(matches);
    }

    @Override
    public Bracket advance(Bracket bracket, Match completedMatch) {
        return bracket;
    }
}
