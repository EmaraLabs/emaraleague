package com.emaralabs.emaraleague.integrations.placeholderapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlaceholderAPIIntegrationTest {

    @Test
    void testPlaceholderAPIIntegrationCreation() {
        PlaceholderAPIIntegration papi = new PlaceholderAPIIntegration(null);
        assertNotNull(papi);
    }

    @Test
    void testPlaceholderAPIIntegrationIsAvailable() {
        PlaceholderAPIIntegration papi = new PlaceholderAPIIntegration(null);
        assertFalse(papi.isAvailable());
    }

    @Test
    void testPlaceholderExpansion() {
        PlaceholderAPIIntegration papi = new PlaceholderAPIIntegration(null);
        EmaraLeagueExpansion expansion = new EmaraLeagueExpansion();
        assertEquals("emaraleague", expansion.getIdentifier());
    }
}
