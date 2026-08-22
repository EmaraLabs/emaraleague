package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchCountdownTest {

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
    void startCountdown_setsRunningState() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});
        assertTrue(countdown.isRunning());
        assertEquals(10, countdown.getRemainingSeconds());
    }

    @Test
    void startCountdown_schedulesRepeatingTask() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});
        verify(scheduler).runRepeating(any(Runnable.class), eq(0L), eq(20L));
    }

    @Test
    void tick_decrementsRemainingSeconds() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        AtomicInteger ticks = new AtomicInteger(0);

        doAnswer(inv -> {
            Runnable task = inv.getArgument(0);
            ticks.incrementAndGet();
            task.run(); // Simulate one tick
            return null;
        }).when(scheduler).runRepeating(any(Runnable.class), eq(0L), eq(20L));

        countdown.startCountdown(match, 10, () -> {});
        assertEquals(9, countdown.getRemainingSeconds());
    }

    @Test
    void countdown_toZero_callsOnComplete() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        boolean[] completed = {false};

        doAnswer(inv -> {
            Runnable task = inv.getArgument(0);
            // Simulate 11 ticks: 10 decrements + 1 final check
            for (int i = 0; i < 11; i++) {
                task.run();
            }
            return null;
        }).when(scheduler).runRepeating(any(Runnable.class), eq(0L), eq(20L));

        countdown.startCountdown(match, 10, () -> completed[0] = true);
        assertTrue(completed[0]);
        assertFalse(countdown.isRunning());
    }

    @Test
    void cancel_stopsCountdown() {
        Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
        countdown.startCountdown(match, 10, () -> {});
        countdown.cancel();
        assertFalse(countdown.isRunning());
        assertEquals(0, countdown.getRemainingSeconds());
    }

    @Test
    void isRunning_default_returnsFalse() {
        assertFalse(countdown.isRunning());
    }

    @Test
    void getRemainingSeconds_default_returnsZero() {
        assertEquals(0, countdown.getRemainingSeconds());
    }
}
