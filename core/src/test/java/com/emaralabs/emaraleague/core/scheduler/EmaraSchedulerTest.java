package com.emaralabs.emaraleague.core.scheduler;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class EmaraSchedulerTest {

    @Test
    void testSchedulerCanRunAsyncTask() {
        EmaraScheduler scheduler = new PaperScheduler(null);
        AtomicBoolean ran = new AtomicBoolean(false);
        scheduler.runAsync(() -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void testSchedulerCanRunDelayedTask() {
        EmaraScheduler scheduler = new PaperScheduler(null);
        AtomicBoolean ran = new AtomicBoolean(false);
        scheduler.runDelayed(() -> ran.set(true), 1);
        assertFalse(ran.get());
    }
}
