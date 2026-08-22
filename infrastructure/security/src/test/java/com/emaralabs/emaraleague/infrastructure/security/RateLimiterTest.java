package com.emaralabs.emaraleague.infrastructure.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void testRateLimiterCreation() {
        RateLimiter limiter = new RateLimiter(5, 1000);
        assertNotNull(limiter);
    }

    @Test
    void testRateLimiterAllow() {
        RateLimiter limiter = new RateLimiter(5, 1000);
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        assertFalse(limiter.allow());
    }

    @Test
    void testRateLimiterReset() {
        RateLimiter limiter = new RateLimiter(2, 100);
        assertTrue(limiter.allow());
        assertTrue(limiter.allow());
        assertFalse(limiter.allow());
        
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertTrue(limiter.allow());
    }
}
