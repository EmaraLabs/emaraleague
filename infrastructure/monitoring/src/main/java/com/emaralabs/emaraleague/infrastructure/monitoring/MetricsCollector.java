package com.emaralabs.emaraleague.infrastructure.monitoring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> timings = new ConcurrentHashMap<>();

    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getCounter(String name) {
        return counters.getOrDefault(name, new AtomicLong(0)).get();
    }

    public void recordTiming(String name, long durationMs) {
        timings.put(name, durationMs);
    }

    public long getTiming(String name) {
        return timings.getOrDefault(name, 0L);
    }

    public Map<String, Object> getAllMetrics() {
        Map<String, Object> all = new ConcurrentHashMap<>();
        counters.forEach((k, v) -> all.put(k, v.get()));
        all.putAll(timings);
        return all;
    }

    public void reset() {
        counters.clear();
        timings.clear();
    }
}
