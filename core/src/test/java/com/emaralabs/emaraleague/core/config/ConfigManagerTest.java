package com.emaralabs.emaraleague.core.config;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigManagerTest {

    private ConfigManager createConfigManager() {
        Plugin mockPlugin = mock(Plugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(new File("build/tmp/test-config"));
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        return new ConfigManager(mockPlugin);
    }

    @Test
    void testConfigManagerCreation() {
        ConfigManager config = createConfigManager();
        assertNotNull(config);
    }

    @Test
    void testDefaultLanguage() {
        ConfigManager config = createConfigManager();
        assertEquals("en", config.getLanguage());
    }

    @Test
    void testDefaultDebugMode() {
        ConfigManager config = createConfigManager();
        assertFalse(config.isDebug());
    }

    @Test
    void testDefaultCountdownSeconds() {
        ConfigManager config = createConfigManager();
        assertEquals(10, config.getCountdownSeconds());
    }

    @Test
    void testDefaultFallThreshold() {
        ConfigManager config = createConfigManager();
        assertEquals(0, config.getFallThreshold());
    }

    @Test
    void testDefaultAutoAssignArena() {
        ConfigManager config = createConfigManager();
        assertTrue(config.isAutoAssignArena());
    }

    @Test
    void testDefaultMode() {
        ConfigManager config = createConfigManager();
        assertEquals("duels", config.getDefaultMode());
    }

    @Test
    void testDefaultMaxConcurrentMatches() {
        ConfigManager config = createConfigManager();
        assertEquals(4, config.getMaxConcurrentMatches());
    }

    @Test
    void testDefaultBossbarCountdown() {
        ConfigManager config = createConfigManager();
        assertTrue(config.isBossbarCountdown());
    }

    @Test
    void testDefaultScoreboardEnabled() {
        ConfigManager config = createConfigManager();
        assertTrue(config.isScoreboardEnabled());
    }
}
