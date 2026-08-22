package com.emaralabs.emaraleague.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmaraLeagueCommandTest {

    @Test
    void testCommandCreation() {
        EmaraLeagueCommand cmd = new EmaraLeagueCommand(null);
        assertNotNull(cmd);
    }

    @Test
    void testCommandName() {
        EmaraLeagueCommand cmd = new EmaraLeagueCommand(null);
        assertEquals("emaraleague", cmd.getName());
    }

    @Test
    void testCommandAliases() {
        EmaraLeagueCommand cmd = new EmaraLeagueCommand(null);
        assertTrue(cmd.getAliases().contains("el"));
        assertTrue(cmd.getAliases().contains("league"));
    }

    @Test
    void testCommandDescription() {
        EmaraLeagueCommand cmd = new EmaraLeagueCommand(null);
        assertEquals("EmaraLeague tournament management", cmd.getDescription());
    }
}
