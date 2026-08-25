package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Premium scoreboard for EmaraLeague matches.
 * Uses Scoreboard Library for smooth, flicker-free updates.
 * Features: animated gradient title, structured layout, per-player tracking.
 */
public final class EmaraScoreboard {

    private final ScoreboardLibrary library;
    private final MatchEngine matchEngine;
    private final Map<UUID, Sidebar> playerSidebars = new HashMap<>();
    private final Map<UUID, GradientAnimator> titleAnimators = new HashMap<>();
    private int timerSeconds = 0;

    public EmaraScoreboard(ScoreboardLibrary library, MatchEngine matchEngine) {
        this.library = library;
        this.matchEngine = matchEngine;
    }

    /**
     * Show scoreboard to a player for a specific match.
     */
    public void showToPlayer(Player player, Match match) {
        Sidebar sidebar = library.createSidebar();
        playerSidebars.put(player.getUniqueId(), sidebar);

        // Create animated title
        GradientAnimator animator = GradientAnimator.goldPulse("⚔ " + getModeDisplayName(match), 8);
        titleAnimators.put(player.getUniqueId(), animator);

        // Build initial layout
        updateLayout(sidebar, player, match, animator);

        // Apply to player
        sidebar.addPlayer(player);
    }

    /**
     * Update scoreboard for a player (call every 5 ticks for animation).
     */
    public void updateForPlayer(Player player, Match match) {
        Sidebar sidebar = playerSidebars.get(player.getUniqueId());
        if (sidebar == null) {
            return;
        }
        GradientAnimator animator = titleAnimators.get(player.getUniqueId());
        updateLayout(sidebar, player, match, animator);
    }

    /**
     * Update all active players (call from scheduler).
     */
    public void updateAll(Match match) {
        for (Map.Entry<UUID, Sidebar> entry : playerSidebars.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                updateForPlayer(player, match);
            }
        }
    }

    /**
     * Hide scoreboard from a player.
     */
    public void hideFromPlayer(Player player) {
        Sidebar sidebar = playerSidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.removePlayer(player);
            sidebar.close();
        }
        titleAnimators.remove(player.getUniqueId());
    }

    /**
     * Hide from all players and cleanup.
     */
    public void hideFromAll() {
        for (Map.Entry<UUID, Sidebar> entry : playerSidebars.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                entry.getValue().removePlayer(player);
            }
            entry.getValue().close();
        }
        playerSidebars.clear();
        titleAnimators.clear();
    }

    /**
     * Set match timer (seconds).
     */
    public void setTimerSeconds(int seconds) {
        this.timerSeconds = seconds;
    }

    /**
     * Get current timer value.
     */
    public int getTimerSeconds() {
        return timerSeconds;
    }

    /**
     * Build the sidebar layout.
     */
    private void updateLayout(Sidebar sidebar, Player player, Match match, GradientAnimator animator) {
        // Animated title (frame updates on each refresh)
        Component title = animator.nextFrame();

        // Build lines with user-friendly colors
        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.text(" "));
        lines.add(Component.text("⏱ ", EmaraTheme.MUTED)
                .append(Component.text(formatTime(timerSeconds), EmaraTheme.TIMER)));
        lines.add(Component.text("🏟 ", EmaraTheme.MUTED)
                .append(Component.text(getArenaName(match), EmaraTheme.ARENA)));
        lines.add(Component.text(" "));
        lines.add(Component.text("▸ " + match.teamA().name(), EmaraTheme.TEAM_A, TextDecoration.BOLD));
        lines.add(Component.text("  " + match.teamA().getPlayerCount() + " players", EmaraTheme.TEXT));
        lines.add(Component.text("  " + getTeamKills(match.teamA()) + " kills", EmaraTheme.STATS));
        lines.add(Component.text("  "));
        lines.add(Component.text("▸ " + match.teamB().name(), EmaraTheme.TEAM_B, TextDecoration.BOLD));
        lines.add(Component.text("  " + match.teamB().getPlayerCount() + " players", EmaraTheme.TEXT));
        lines.add(Component.text("  " + getTeamKills(match.teamB()) + " kills", EmaraTheme.STATS));
        lines.add(Component.text(" "));
        lines.add(buildStateLine(match));

        // Apply to sidebar using ComponentSidebarLayout
        SidebarComponent titleComponent = SidebarComponent.staticLine(title);
        SidebarComponent.Builder linesBuilder = SidebarComponent.builder();
        for (Component line : lines) {
            linesBuilder.addStaticLine(line);
        }
        ComponentSidebarLayout layout = new ComponentSidebarLayout(titleComponent, linesBuilder.build());
        layout.apply(sidebar);
    }

    /**
     * Build state indicator line with user-friendly text.
     */
    private Component buildStateLine(Match match) {
        return switch (match.state()) {
            case PENDING -> Component.text("⏳ ", EmaraTheme.MUTED)
                    .append(Component.text("Waiting...", EmaraTheme.MUTED));
            case STARTING -> Component.text("▶ ", EmaraTheme.WARNING)
                    .append(Component.text("Get Ready!", EmaraTheme.WARNING, TextDecoration.BOLD));
            case INGAME -> Component.text("🔥 ", EmaraTheme.SUCCESS)
                    .append(Component.text("Match in Progress", EmaraTheme.SUCCESS, TextDecoration.BOLD));
            case ENDED -> {
                if (match.winner() != null) {
                    yield Component.text("🏆 ", EmaraTheme.PRIMARY)
                            .append(Component.text(match.winner().name() + " Wins!", EmaraTheme.PRIMARY, TextDecoration.BOLD));
                }
                yield Component.text("■ ", EmaraTheme.INFO)
                        .append(Component.text("Match Ended", EmaraTheme.INFO));
            }
        };
    }

    /**
     * Get display name for game mode.
     */
    private String getModeDisplayName(Match match) {
        java.util.UUID tournamentId = matchEngine.getMatchToTournament().get(match.id());
        if (tournamentId != null) {
            return matchEngine.getTournamentManager().getTournament(tournamentId)
                    .map(t -> t.mode().toUpperCase().replace("-", " "))
                    .orElse("MATCH");
        }
        return "MATCH";
    }

    /**
     * Get arena name for match.
     */
    private String getArenaName(Match match) {
        java.util.UUID arenaId = matchEngine.getMatchToArena().get(match.id());
        if (arenaId != null) {
            return matchEngine.getArenaManager().getArena(arenaId)
                    .map(a -> a.getName())
                    .orElse("Unknown");
        }
        return "Unknown";
    }

    /**
     * Get total kills for a team.
     */
    private int getTeamKills(Team team) {
        if (matchEngine.getPlayerStats() == null) {
            return 0;
        }
        int total = 0;
        for (java.util.UUID playerId : team.playerIds()) {
            total += matchEngine.getPlayerStats().getKills(playerId);
        }
        return total;
    }

    /**
     * Format seconds to MM:SS.
     */
    public String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Check if player has active scoreboard.
     */
    public boolean hasScoreboard(Player player) {
        return playerSidebars.containsKey(player.getUniqueId());
    }

    /**
     * Get active player count.
     */
    public int getActivePlayerCount() {
        return playerSidebars.size();
    }
}
