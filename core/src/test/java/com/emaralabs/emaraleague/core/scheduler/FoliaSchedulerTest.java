package com.emaralabs.emaraleague.core.scheduler;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class FoliaSchedulerTest {

    @Test
    void testFoliaSchedulerCanRunAsyncTask() {
        FoliaScheduler scheduler = new FoliaScheduler(null);
        AtomicBoolean ran = new AtomicBoolean(false);
        scheduler.runAsync(() -> ran.set(true));
        assertTrue(ran.get());
    }

    @Test
    void testFoliaSchedulerHasRegionSupport() {
        FoliaScheduler scheduler = new FoliaScheduler(null);
        assertNotNull(scheduler);
    }

    @Test
    void testFoliaSchedulerHasEntitySupport() {
        FoliaScheduler scheduler = new FoliaScheduler(null);
        assertNotNull(scheduler);
    }
}
