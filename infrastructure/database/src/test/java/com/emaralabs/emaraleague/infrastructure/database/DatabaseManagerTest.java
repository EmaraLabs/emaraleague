package com.emaralabs.emaraleague.infrastructure.database;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void testDatabaseManagerCreation() {
        DatabaseManager db = new DatabaseManager("jdbc:sqlite::memory:", "", "");
        assertNotNull(db);
        db.close();
    }

    @Test
    void testSchemaInitialization() {
        DatabaseManager db = new DatabaseManager("jdbc:sqlite::memory:", "", "");
        db.initializeSchema();
        db.close();
    }
}
