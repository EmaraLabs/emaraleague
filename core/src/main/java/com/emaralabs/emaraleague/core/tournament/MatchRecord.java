package com.emaralabs.emaraleague.core.tournament;

import java.util.UUID;

public record MatchRecord(
    UUID matchId,
    String tournamentName,
    String mode,
    String teamAName,
    String teamBName,
    String winnerName,
    long timestamp
) {
    public static MatchRecord fromMatch(Match match, String tournamentName, String mode) {
        return new MatchRecord(
            match.id(),
            tournamentName,
            mode,
            match.teamA().name(),
            match.teamB().name(),
            match.winner() != null ? match.winner().name() : "Draw",
            System.currentTimeMillis()
        );
    }
}
