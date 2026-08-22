package com.emaralabs.emaraleague.infrastructure.security;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    private final int maxRequests;
    private final long timeWindowMs;
    private final AtomicInteger requestCount;
    private final AtomicLong windowStart;

    public RateLimiter(int maxRequests, long timeWindowMs) {
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeWindowMs;
        this.requestCount = new AtomicInteger(0);
        this.windowStart = new AtomicLong(System.currentTimeMillis());
    }

    public boolean allow() {
        long now = System.currentTimeMillis();
        long windowStartTime = windowStart.get();
        
        if (now - windowStartTime > timeWindowMs) {
            windowStart.set(now);
            requestCount.set(0);
        }
        
        return requestCount.incrementAndGet() <= maxRequests;
    }

    public int getRemainingRequests() {
        return Math.max(0, maxRequests - requestCount.get());
    }

    public void reset() {
        requestCount.set(0);
        windowStart.set(System.currentTimeMillis());
    }
}
