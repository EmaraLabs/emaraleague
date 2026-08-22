package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.BracketGenerator;
import com.emaralabs.emaraleague.core.game.GameModeRegistry;
import com.emaralabs.emaraleague.core.tournament.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchEngine {

    private final TournamentManager tournaments;
    private final ArenaManager arenas;
    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> matchToTournament = new ConcurrentHashMap<>();
    private GameModeRegistry gameModeRegistry;
    private MatchCountdown countdown;

    public MatchEngine(TournamentManager tournaments, ArenaManager arenas) {
        this.tournaments = tournaments;
        this.arenas = arenas;
    }

    public void setGameModeRegistry(GameModeRegistry registry) {
        this.gameModeRegistry = registry;
    }

    public void setCountdown(MatchCountdown countdown) {
        this.countdown = countdown;
    }

    public Match createMatch(String tournamentName, Team teamA, Team teamB) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

        Match match = new Match(teamA, teamB);
        matches.put(match.id(), match);
        matchToTournament.put(match.id(), tournament.id());
        return match;
    }

    public Match startMatch(UUID matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        validateMatchTransition(match.state(), MatchState.STARTING);
        Match updated = match.withState(MatchState.STARTING);
        matches.put(matchId, updated);

        if (gameModeRegistry != null) {
            UUID tournamentId = matchToTournament.get(matchId);
            tournaments.getTournament(tournamentId).ifPresent(t ->
                    gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchStart(updated)));
        }

        if (countdown != null) {
            countdown.startCountdown(updated, 10, () -> beginPlay(matchId));
        }

        return updated;
    }

    public Match beginPlay(UUID matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        validateMatchTransition(match.state(), MatchState.INGAME);
        Match updated = match.withState(MatchState.INGAME);
        matches.put(matchId, updated);
        return updated;
    }

    public Match endMatch(UUID matchId, Team winner) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        validateMatchTransition(match.state(), MatchState.ENDED);
        Match updated = match.withWinner(winner);
        matches.put(matchId, updated);

        if (gameModeRegistry != null) {
            UUID tournamentId = matchToTournament.get(matchId);
            tournaments.getTournament(tournamentId).ifPresent(t ->
                    gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchEnd(updated, winner)));
        }

        return updated;
    }

    public Optional<Match> getMatch(UUID matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    public List<Match> getMatches(String tournamentName) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));
        return matches.values().stream()
                .filter(m -> tournament.id().equals(matchToTournament.get(m.id())))
                .toList();
    }

    public List<Match> getMatchesByState(String tournamentName, MatchState state) {
        return getMatches(tournamentName).stream()
                .filter(m -> m.state() == state)
                .toList();
    }

    public int getActiveMatchCount(String tournamentName) {
        return (int) getMatches(tournamentName).stream()
                .filter(m -> m.state() == MatchState.STARTING || m.state() == MatchState.INGAME)
                .count();
    }

    public Bracket generateBracket(String tournamentName, BracketGenerator generator) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

        if (tournament.teams().size() < 2) {
            throw new IllegalStateException("Need at least 2 teams to generate bracket");
        }

        Bracket bracket = generator.generate(tournament.teams());

        for (Match match : bracket.getMatches()) {
            if (match.teamA() != null && match.teamB() != null) {
                matches.put(match.id(), match);
                matchToTournament.put(match.id(), tournament.id());
            }
        }

        return bracket;
    }

    public Optional<Match> getNextMatch(String tournamentName) {
        return getMatchesByState(tournamentName, MatchState.PENDING).stream()
                .findFirst();
    }

    private void validateMatchTransition(MatchState current, MatchState next) {
        boolean valid = switch (current) {
            case PENDING -> next == MatchState.STARTING;
            case STARTING -> next == MatchState.INGAME;
            case INGAME -> next == MatchState.ENDED;
            case ENDED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid match transition: %s -> %s", current, next)
            );
        }
    }
}
