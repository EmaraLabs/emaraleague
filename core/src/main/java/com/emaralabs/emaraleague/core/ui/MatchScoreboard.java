package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MatchScoreboard {

    private final MatchEngine matchEngine;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

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

    private void updateBoard(Objective obj, Match match) {
        // Clear existing scores
        for (String entry : obj.getScoreboard().getEntries()) {
            obj.getScoreboard().resetScores(entry);
        }

        int line = 15;

        // Match state
        obj.getScore(PLAIN.serialize(Component.text("State: ", EmaraTheme.MUTED)
                .append(Component.text(match.state().name(), stateColor(match.state())))))
                .setScore(line--);

        obj.getScore(" ").setScore(line--);

        // Team A
        Team teamA = match.teamA();
        obj.getScore(PLAIN.serialize(Component.text(teamA.name(), EmaraTheme.ACCENT, TextDecoration.BOLD)))
                .setScore(line--);
        obj.getScore(PLAIN.serialize(Component.text("  Players: " + teamA.getPlayerCount(), EmaraTheme.INFO)))
                .setScore(line--);

        obj.getScore("  ").setScore(line--);

        // Team B
        Team teamB = match.teamB();
        obj.getScore(PLAIN.serialize(Component.text(teamB.name(), EmaraTheme.ACCENT, TextDecoration.BOLD)))
                .setScore(line--);
        obj.getScore(PLAIN.serialize(Component.text("  Players: " + teamB.getPlayerCount(), EmaraTheme.INFO)))
                .setScore(line--);

        // Winner (if ended)
        if (match.state() == MatchState.ENDED && match.winner() != null) {
            obj.getScore("   ").setScore(line--);
            obj.getScore(PLAIN.serialize(Component.text("Winner: ", EmaraTheme.MUTED)
                    .append(Component.text(match.winner().name(), EmaraTheme.SUCCESS, TextDecoration.BOLD))))
                    .setScore(line--);
        }
    }

    private net.kyori.adventure.text.format.TextColor stateColor(MatchState state) {
        return switch (state) {
            case PENDING -> EmaraTheme.MUTED;
            case STARTING -> EmaraTheme.WARNING;
            case INGAME -> EmaraTheme.SUCCESS;
            case ENDED -> EmaraTheme.INFO;
        };
    }
}
