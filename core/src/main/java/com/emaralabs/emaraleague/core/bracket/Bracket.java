package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;

import java.util.List;

public class Bracket {

    private final List<Match> matches;
    private final int totalMatches;

    public Bracket(List<Match> matches) {
        this.matches = matches;
        this.totalMatches = matches.size();
    }

    public List<Match> getMatches() {
        return matches;
    }

    public int getTotalMatches() {
        return totalMatches;
    }
}
