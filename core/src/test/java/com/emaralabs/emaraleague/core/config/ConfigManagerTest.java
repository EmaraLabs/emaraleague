package com.emaralabs.emaraleague.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {

    @Test
    void testConfigManagerCreation() {
        ConfigManager config = new ConfigManager(null);
        assertNotNull(config);
    }

    @Test
    void testDefaultLanguage() {
        ConfigManager config = new ConfigManager(null);
        assertEquals("en", config.getLanguage());
    }

    @Test
    void testDefaultDebugMode() {
        ConfigManager config = new ConfigManager(null);
        assertFalse(config.isDebug());
    }
}
