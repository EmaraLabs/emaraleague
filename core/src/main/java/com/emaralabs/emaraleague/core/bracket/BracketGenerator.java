package com.emaralabs.emaraleague.core.bracket;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.List;

public interface BracketGenerator {

    Bracket generate(List<Team> teams);

    Bracket advance(Bracket bracket, Match completedMatch);
}
