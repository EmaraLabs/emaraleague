package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchCountdownBossBarTest {

    private EmaraScheduler scheduler;
    private MessageRegistry messages;
    private MatchCountdown countdown;

    @BeforeEach
    void setUp() {
        scheduler = mock(EmaraScheduler.class);
        messages = mock(MessageRegistry.class);
        countdown = new MatchCountdown(scheduler, messages);
    }

    @Test
    void startCountdown_createsBossBar() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});
        assertNotNull(countdown.getBossBar());
    }

    @Test
    void startCountdown_showsBossBarToPlayers() {
        Player player = mock(Player.class);
        countdown.addPlayer(player);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});

        verify(player).showBossBar(any(BossBar.class));
    }

    @Test
    void updateBossBar_progressDecreases() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});

        BossBar bar = countdown.getBossBar();
        assertNotNull(bar);
        assertEquals(1.0f, bar.progress(), 0.01f);

        // Simulate one tick
        countdown.tick();
        assertEquals(0.9f, bar.progress(), 0.01f);
    }

    @Test
    void updateBossBar_colorChangesAtThresholds() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});

        BossBar bar = countdown.getBossBar();
        assertNotNull(bar);
        assertEquals(BossBar.Color.YELLOW, bar.color());

        // Simulate to 5 seconds
        for (int i = 0; i < 5; i++) {
            countdown.tick();
        }
        assertEquals(BossBar.Color.YELLOW, bar.color());

        // Simulate to 3 seconds
        countdown.tick();
        countdown.tick();
        assertEquals(BossBar.Color.RED, bar.color());
    }

    @Test
    void cancel_hidesBossBar() {
        Player player = mock(Player.class);
        countdown.addPlayer(player);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});
        countdown.cancel();

        verify(player).hideBossBar(any(BossBar.class));
        assertNull(countdown.getBossBar());
    }

    @Test
    void addPlayer_showsBossBarIfActive() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});

        Player player = mock(Player.class);
        countdown.addPlayer(player);

        verify(player).showBossBar(any(BossBar.class));
    }

    @Test
    void removePlayer_hidesBossBar() {
        Player player = mock(Player.class);
        countdown.addPlayer(player);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});

        countdown.removePlayer(player);
        verify(player).hideBossBar(any(BossBar.class));
    }

    @Test
    void clearPlayers_removesAll() {
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        countdown.addPlayer(p1);
        countdown.addPlayer(p2);

        countdown.clearPlayers();
        assertEquals(0, countdown.getPlayerCount());
    }

    @Test
    void countdownComplete_hidesBossBar() {
        Player player = mock(Player.class);
        countdown.addPlayer(player);

        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        boolean[] completed = {false};

        doAnswer(inv -> {
            Runnable task = inv.getArgument(0);
            for (int i = 0; i < 11; i++) {
                task.run();
            }
            return null;
        }).when(scheduler).runRepeating(any(Runnable.class), eq(0L), eq(20L));

        countdown.startCountdown(match, 10, () -> completed[0] = true);

        assertTrue(completed[0]);
        assertNull(countdown.getBossBar());
    }
}
