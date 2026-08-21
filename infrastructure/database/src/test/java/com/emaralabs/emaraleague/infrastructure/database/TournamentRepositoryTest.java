package com.emaralabs.emaraleague.infrastructure.database;

import com.emaralabs.emaraleague.core.tournament.BracketType;
import com.emaralabs.emaraleague.core.tournament.Tournament;
import com.emaralabs.emaraleague.core.tournament.TournamentState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TournamentRepositoryTest {

    private DatabaseManager db;
    private TournamentRepository repo;

    @BeforeEach
    void setUp() {
        db = new DatabaseManager("jdbc:sqlite::memory:", "", "");
        db.initializeSchema();
        repo = new TournamentRepository(db);
    }

    @AfterEach
    void tearDown() {
        repo.shutdown();
        db.close();
    }

    @Test
    void testSaveAndFindById() {
        Tournament tournament = new Tournament("Test Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament saved = repo.save(tournament).join();
        assertNotNull(saved);

        Optional<Tournament> found = repo.findById(tournament.id()).join();
        assertTrue(found.isPresent());
        assertEquals("Test Cup", found.get().name());
    }

    @Test
    void testFindAll() {
        Tournament t1 = new Tournament("Cup 1", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t2 = new Tournament("Cup 2", "spleef", BracketType.ROUND_ROBIN);
        
        repo.save(t1).join();
        repo.save(t2).join();

        List<Tournament> all = repo.findAll().join();
        assertEquals(2, all.size());
    }

    @Test
    void testDelete() {
        Tournament tournament = new Tournament("To Delete", "duels", BracketType.SINGLE_ELIMINATION);
        repo.save(tournament).join();

        repo.delete(tournament.id()).join();

        Optional<Tournament> found = repo.findById(tournament.id()).join();
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdate() {
        Tournament tournament = new Tournament("Original", "duels", BracketType.SINGLE_ELIMINATION);
        repo.save(tournament).join();

        Tournament updated = tournament.withState(TournamentState.IN_PROGRESS);
        repo.update(updated).join();

        Optional<Tournament> found = repo.findById(tournament.id()).join();
        assertTrue(found.isPresent());
        assertEquals(TournamentState.IN_PROGRESS, found.get().state());
    }
}
