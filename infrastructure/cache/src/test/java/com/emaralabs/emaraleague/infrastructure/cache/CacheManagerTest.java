package com.emaralabs.emaraleague.infrastructure.cache;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CacheManagerTest {

    @Test
    void testCachePutAndGet() {
        CacheManager cache = new CacheManager();
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    void testCacheGetNonExistent() {
        CacheManager cache = new CacheManager();
        assertNull(cache.get("nonexistent"));
    }

    @Test
    void testCacheInvalidate() {
        CacheManager cache = new CacheManager();
        cache.put("key1", "value1");
        cache.invalidate("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    void testCacheSize() {
        CacheManager cache = new CacheManager();
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        assertEquals(2, cache.size());
    }
}
