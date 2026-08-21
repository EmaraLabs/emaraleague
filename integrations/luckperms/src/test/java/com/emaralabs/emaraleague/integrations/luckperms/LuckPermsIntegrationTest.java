package com.emaralabs.emaraleague.integrations.luckperms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsIntegrationTest {

    @Test
    void testLuckPermsIntegrationCreation() {
        LuckPermsIntegration luckPerms = new LuckPermsIntegration(null);
        assertNotNull(luckPerms);
    }

    @Test
    void testLuckPermsIntegrationIsAvailable() {
        LuckPermsIntegration luckPerms = new LuckPermsIntegration(null);
        assertFalse(luckPerms.isAvailable());
    }

    @Test
    void testLuckPermsIntegrationGetApi() {
        LuckPermsIntegration luckPerms = new LuckPermsIntegration(null);
        assertNull(luckPerms.getApi());
    }
}
