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
        List<Match> matches = new ArrayList<>(bracket.getMatches());
        Team winner = completedMatch.winner();
        if (winner == null) {
            return bracket;
        }

        // Find the completed match index
        int completedIndex = -1;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).id().equals(completedMatch.id())) {
                completedIndex = i;
                break;
            }
        }

        if (completedIndex < 0) {
            return bracket;
        }

        // Update the completed match with winner
        matches.set(completedIndex, completedMatch);

        // Calculate next match index for winner
        int nextMatchIndex = getNextMatchIndex(completedIndex, matches.size());
        if (nextMatchIndex >= 0 && nextMatchIndex < matches.size()) {
            Match nextMatch = matches.get(nextMatchIndex);
            Team teamA = nextMatch.teamA();
            Team teamB = nextMatch.teamB();

            // Place winner in empty slot
            if (teamA == null && teamB == null) {
                matches.set(nextMatchIndex, new Match(winner, null));
            } else if (teamA == null) {
                matches.set(nextMatchIndex, new Match(winner, teamB));
            } else if (teamB == null) {
                matches.set(nextMatchIndex, new Match(teamA, winner));
            }
        }

        return new Bracket(matches);
    }

    private int getNextMatchIndex(int completedIndex, int totalMatches) {
        if (totalMatches <= 1) {
            return -1;
        }
        if (totalMatches == 3) {
            if (completedIndex == 0 || completedIndex == 1) {
                return 2;
            }
            return -1;
        }
        int round = (int) (Math.log(completedIndex + 1) / Math.log(2));
        int positionInRound = completedIndex - ((1 << round) - 1);
        int nextRoundStart = ((1 << (round + 1)) - 1);
        return nextRoundStart + positionInRound / 2;
    }

    private int getFirstRoundMatchCount(int teamCount) {
        int powerOfTwo = 1;
        while (powerOfTwo * 2 <= teamCount) {
            powerOfTwo *= 2;
        }
        return powerOfTwo / 2;
    }
}
