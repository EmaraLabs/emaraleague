package com.emaralabs.emaraleague.infrastructure.monitoring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetricsCollectorTest {

    @Test
    void testMetricsCollectorCreation() {
        MetricsCollector metrics = new MetricsCollector();
        assertNotNull(metrics);
    }

    @Test
    void testIncrementCounter() {
        MetricsCollector metrics = new MetricsCollector();
        metrics.incrementCounter("tournaments.created");
        metrics.incrementCounter("tournaments.created");
        assertEquals(2, metrics.getCounter("tournaments.created"));
    }

    @Test
    void testRecordTiming() {
        MetricsCollector metrics = new MetricsCollector();
        metrics.recordTiming("match.duration", 1500);
        assertEquals(1500, metrics.getTiming("match.duration"));
    }

    @Test
    void testGetAllMetrics() {
        MetricsCollector metrics = new MetricsCollector();
        metrics.incrementCounter("test.counter");
        metrics.recordTiming("test.timing", 100);
        
        assertTrue(metrics.getAllMetrics().containsKey("test.counter"));
        assertTrue(metrics.getAllMetrics().containsKey("test.timing"));
    }
}
