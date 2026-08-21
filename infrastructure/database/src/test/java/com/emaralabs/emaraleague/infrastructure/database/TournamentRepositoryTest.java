package com.emaralabs.emaraleague.infrastructure.database;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;

class TournamentRepositoryTest {

    @Test
    void testRepositoryCreation() {
        TournamentRepository repo = new TournamentRepository();
        assertNotNull(repo);
    }

    @Test
    void testFindAllEmpty() {
        TournamentRepository repo = new TournamentRepository();
        CompletableFuture<List<Object>> future = repo.findAll();
        List<Object> results = future.join();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
