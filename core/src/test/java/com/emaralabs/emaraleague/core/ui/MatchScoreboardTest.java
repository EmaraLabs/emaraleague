package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.MatchState;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchScoreboardTest {

    private MatchScoreboard scoreboard;
    private MatchEngine matchEngine;

    @BeforeEach
    void setUp() {
        matchEngine = mock(MatchEngine.class);
        scoreboard = new MatchScoreboard(matchEngine);
    }

    @Test
    void formatTime_zeroSeconds_returns0000() {
        assertEquals("00:00", scoreboard.formatTime(0));
    }

    @Test
    void formatTime_oneMinute_returns0100() {
        assertEquals("01:00", scoreboard.formatTime(60));
    }

    @Test
    void formatTime_twoMinutesThirtyFive_returns0235() {
        assertEquals("02:35", scoreboard.formatTime(155));
    }

    @Test
    void formatTime_tenMinutes_returns1000() {
        assertEquals("10:00", scoreboard.formatTime(600));
    }

    @Test
    void formatTime_fiftyNineSeconds_returns0059() {
        assertEquals("00:59", scoreboard.formatTime(59));
    }

    @Test
    void setTimerSeconds_storesValue() {
        scoreboard.setTimerSeconds(155);
        assertEquals(155, scoreboard.getTimerSeconds());
    }

    @Test
    void stateColor_pending_muted() {
        assertEquals(EmaraTheme.MUTED, scoreboard.stateColor(MatchState.PENDING));
    }

    @Test
    void stateColor_starting_warning() {
        assertEquals(EmaraTheme.WARNING, scoreboard.stateColor(MatchState.STARTING));
    }

    @Test
    void stateColor_ingame_success() {
        assertEquals(EmaraTheme.SUCCESS, scoreboard.stateColor(MatchState.INGAME));
    }

    @Test
    void stateColor_ended_info() {
        assertEquals(EmaraTheme.INFO, scoreboard.stateColor(MatchState.ENDED));
    }

    @Test
    void buildTeamLines_withKills_includesKillCount() {
        Team team = new Team("Alpha", 1);
        Map<UUID, Integer> kills = new HashMap<>();
        kills.put(UUID.randomUUID(), 3);

        var lines = scoreboard.buildTeamLines(team, kills);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Kills: 3")));
    }

    @Test
    void buildTeamLines_noKills_showsZero() {
        Team team = new Team("Alpha", 1);
        Map<UUID, Integer> kills = new HashMap<>();

        var lines = scoreboard.buildTeamLines(team, kills);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Kills: 0")));
    }

    @Test
    void buildTeamLines_eliminatedPlayers_showsRed() {
        Team team = new Team("Alpha", 1);
        // Team with 0 players should show red
        var lines = scoreboard.buildTeamLines(team, new HashMap<>());
        assertTrue(lines.stream().anyMatch(l -> l.contains("Players: 0")));
    }

    @Test
    void scoreboard_maxLines_doesNotOverflow() {
        Team a = new Team("Alpha", 1);
        Team b = new Team("Beta", 2);
        Match match = new Match(a, b);

        var lines = scoreboard.buildAllLines(match, new HashMap<>(), new HashMap<>(), 0);
        assertTrue(lines.size() <= 15, "Scoreboard should not exceed 15 lines");
    }
}
