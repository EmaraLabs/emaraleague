package com.emaralabs.emaraleague.infrastructure.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaTest {

    @Test
    void testSchemaConstants() {
        assertEquals("tournaments", DatabaseSchema.getTournamentsTable());
        assertEquals("matches", DatabaseSchema.getMatchesTable());
        assertEquals("teams", DatabaseSchema.getTeamsTable());
        assertEquals("players", DatabaseSchema.getPlayersTable());
    }
}
