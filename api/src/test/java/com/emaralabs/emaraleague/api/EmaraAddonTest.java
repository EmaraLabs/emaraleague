package com.emaralabs.emaraleague.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmaraAddonTest {

    @Test
    void addon_hasRequiredMethods() {
        EmaraAddon addon = new EmaraAddon() {
            @Override public String getId() { return "test-addon"; }
            @Override public String getName() { return "Test Addon"; }
            @Override public String getVersion() { return "1.0.0"; }
            @Override public int getRequiredApiVersion() { return 1; }
            @Override public void onEnable(EmaraLeagueAPI api) { }
            @Override public void onDisable() { }
        };

        assertEquals("test-addon", addon.getId());
        assertEquals("Test Addon", addon.getName());
        assertEquals("1.0.0", addon.getVersion());
        assertEquals(1, addon.getRequiredApiVersion());
    }

    @Test
    void addon_onEnable_receivesApi() {
        EmaraLeagueAPI mockApi = new EmaraLeagueAPI() {
            @Override public int getApiVersion() { return 1; }
            @Override public Object getTournamentManager() { return null; }
            @Override public Object getArenaManager() { return null; }
            @Override public Object getMatchEngine() { return null; }
            @Override public Object getGameModeRegistry() { return null; }
            @Override public Object getPlayerSessionManager() { return null; }
            @Override public void registerAddon(EmaraAddon addon) { }
            @Override public void unregisterAddon(String addonId) { }
            @Override public java.util.List<EmaraAddon> getAddons() { return java.util.List.of(); }
            @Override public boolean isAddonEnabled(String addonId) { return false; }
            @Override public void registerGameMode(Object gameMode) { }
            @Override public void unregisterGameMode(String gameModeId) { }
            @Override public void broadcastToTournament(String tournamentName, net.kyori.adventure.text.Component message) { }
            @Override public void broadcastToMatch(java.util.UUID matchId, net.kyori.adventure.text.Component message) { }
        };

        boolean[] enabled = {false};
        EmaraAddon addon = new EmaraAddon() {
            @Override public String getId() { return "test"; }
            @Override public String getName() { return "Test"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public int getRequiredApiVersion() { return 1; }
            @Override public void onEnable(EmaraLeagueAPI api) { enabled[0] = true; }
            @Override public void onDisable() { }
        };

        addon.onEnable(mockApi);
        assertTrue(enabled[0]);
    }

    @Test
    void addon_onDisable_called() {
        boolean[] disabled = {false};
        EmaraAddon addon = new EmaraAddon() {
            @Override public String getId() { return "test"; }
            @Override public String getName() { return "Test"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public int getRequiredApiVersion() { return 1; }
            @Override public void onEnable(EmaraLeagueAPI api) { }
            @Override public void onDisable() { disabled[0] = true; }
        };

        addon.onDisable();
        assertTrue(disabled[0]);
    }
}
