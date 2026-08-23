package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MatchScoreboard {

    private final MatchEngine matchEngine;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private int timerSeconds = 0;

    public MatchScoreboard(MatchEngine matchEngine) {
        this.matchEngine = matchEngine;
    }

    public void showToPlayer(Player player, Match match) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("match", Criteria.DUMMY,
                Component.text("EmaraLeague", EmaraTheme.PRIMARY, TextDecoration.BOLD));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateBoard(obj, match);

        player.setScoreboard(board);
        playerBoards.put(player.getUniqueId(), board);
    }

    public void updateForPlayer(Player player, Match match) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) {
            return;
        }
        Objective obj = board.getObjective("match");
        if (obj == null) {
            return;
        }
        updateBoard(obj, match);
    }

    public void hideFromPlayer(Player player) {
        playerBoards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void hideFromAll() {
        for (UUID playerId : playerBoards.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        playerBoards.clear();
    }

    public void setTimerSeconds(int seconds) {
        this.timerSeconds = seconds;
    }

    public int getTimerSeconds() {
        return timerSeconds;
    }

    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    public TextColor stateColor(MatchState state) {
        return switch (state) {
            case PENDING -> EmaraTheme.MUTED;
            case STARTING -> EmaraTheme.WARNING;
            case INGAME -> EmaraTheme.SUCCESS;
            case ENDED -> EmaraTheme.INFO;
        };
    }

    public List<String> buildTeamLines(Team team, Map<UUID, Integer> stats) {
        List<String> lines = new ArrayList<>();
        lines.add(PLAIN.serialize(Component.text(team.name(), EmaraTheme.ACCENT, TextDecoration.BOLD)));

        TextColor playerColor = team.getPlayerCount() > 0 ? EmaraTheme.SUCCESS : EmaraTheme.ERROR;
        lines.add(PLAIN.serialize(Component.text("  Players: " + team.getPlayerCount(), playerColor)));

        int totalStats = stats.values().stream().mapToInt(Integer::intValue).sum();
        lines.add(PLAIN.serialize(Component.text("  Kills: " + totalStats, EmaraTheme.PRIMARY)));

        return lines;
    }

    public List<String> buildAllLines(Match match, Map<UUID, Integer> teamAStats,
                                       Map<UUID, Integer> teamBStats, int timerSecs) {
        List<String> lines = new ArrayList<>();

        lines.add(PLAIN.serialize(Component.text("State: ", EmaraTheme.MUTED)
                .append(Component.text(match.state().name(), stateColor(match.state())))));

        if (timerSecs > 0) {
            lines.add(PLAIN.serialize(Component.text("Time: ", EmaraTheme.MUTED)
                    .append(Component.text(formatTime(timerSecs), EmaraTheme.INFO))));
        }

        lines.add(" ");

        lines.addAll(buildTeamLines(match.teamA(), teamAStats));
        lines.add("  ");
        lines.addAll(buildTeamLines(match.teamB(), teamBStats));

        if (match.state() == MatchState.ENDED && match.winner() != null) {
            lines.add("   ");
            lines.add(PLAIN.serialize(Component.text("Winner: ", EmaraTheme.MUTED)
                    .append(Component.text(match.winner().name(), EmaraTheme.SUCCESS, TextDecoration.BOLD))));
        }

        return lines;
    }

    private void updateBoard(Objective obj, Match match) {
        // Clear existing scores
        for (String entry : obj.getScoreboard().getEntries()) {
            obj.getScoreboard().resetScores(entry);
        }

        List<String> lines = buildAllLines(match, new HashMap<>(), new HashMap<>(), timerSeconds);

        int line = 15;
        for (String text : lines) {
            if (line < 0) break;
            obj.getScore(text).setScore(line--);
        }
    }
}
