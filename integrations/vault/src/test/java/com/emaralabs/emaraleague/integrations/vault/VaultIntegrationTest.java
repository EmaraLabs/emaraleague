package com.emaralabs.emaraleague.integrations.vault;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VaultIntegrationTest {

    @Test
    void testVaultIntegrationCreation() {
        VaultIntegration vault = new VaultIntegration(null);
        assertNotNull(vault);
    }

    @Test
    void testVaultIntegrationIsAvailable() {
        VaultIntegration vault = new VaultIntegration(null);
        assertFalse(vault.isAvailable());
    }

    @Test
    void testVaultIntegrationGetEconomy() {
        VaultIntegration vault = new VaultIntegration(null);
        assertNull(vault.getEconomy());
    }
}
