package com.emaralabs.emaraleague.core.tournament;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class TournamentManagerPersistenceTest {

    private TournamentManager manager;
    private TournamentPersistence persistence;

    @BeforeEach
    void setUp() {
        manager = new TournamentManager();
        persistence = new InMemoryTournamentPersistence();
        manager.setPersistence(persistence);
    }

    @Test
    void createTournament_withPersistence_savesToDb() {
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t = manager.getTournament("SummerCup").get();
        Optional<Tournament> fromDb = persistence.findById(t.id()).join();
        assertTrue(fromDb.isPresent());
        assertEquals("SummerCup", fromDb.get().name());
    }

    @Test
    void deleteTournament_withPersistence_removesFromDb() {
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t = manager.getTournament("SummerCup").get();
        manager.deleteTournament("SummerCup");
        Optional<Tournament> fromDb = persistence.findById(t.id()).join();
        assertTrue(fromDb.isEmpty());
    }

    @Test
    void transitionState_withPersistence_updatesDb() {
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t = manager.getTournament("SummerCup").get();
        manager.transitionState("SummerCup", TournamentState.STARTING);
        Optional<Tournament> fromDb = persistence.findById(t.id()).join();
        assertTrue(fromDb.isPresent());
        assertEquals(TournamentState.STARTING, fromDb.get().state());
    }

    @Test
    void loadFromDatabase_restoresTournaments() {
        Tournament t1 = new Tournament("Cup1", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t2 = new Tournament("Cup2", "spleef", BracketType.ROUND_ROBIN);
        persistence.save(t1).join();
        persistence.save(t2).join();

        manager.loadFromDatabase();
        assertEquals(2, manager.count());
        assertTrue(manager.exists("Cup1"));
        assertTrue(manager.exists("Cup2"));
    }

    @Test
    void loadFromDatabase_emptyDb_startsEmpty() {
        manager.loadFromDatabase();
        assertEquals(0, manager.count());
    }

    @Test
    void noPersistence_worksAsBefore() {
        TournamentManager noPersist = new TournamentManager();
        noPersist.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertTrue(noPersist.exists("Cup"));
        assertEquals(1, noPersist.count());
    }

    @Test
    void addTeam_withPersistence_updatesDb() {
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t = manager.getTournament("Cup").get();
        manager.addTeam("Cup", new Team("Alpha", 1));
        Optional<Tournament> fromDb = persistence.findById(t.id()).join();
        assertTrue(fromDb.isPresent());
        assertEquals(1, fromDb.get().teams().size());
    }

    @Test
    void removeTeam_withPersistence_updatesDb() {
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament t = manager.getTournament("Cup").get();
        Team team = new Team("Alpha", 1);
        manager.addTeam("Cup", team);
        manager.removeTeam("Cup", team.id());
        Optional<Tournament> fromDb = persistence.findById(t.id()).join();
        assertTrue(fromDb.isPresent());
        assertEquals(0, fromDb.get().teams().size());
    }

    // ── In-Memory Persistence for Testing ───────────────────────────

    static class InMemoryTournamentPersistence implements TournamentPersistence {
        private final java.util.Map<UUID, Tournament> store = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public CompletableFuture<Tournament> save(Tournament tournament) {
            store.put(tournament.id(), tournament);
            return CompletableFuture.completedFuture(tournament);
        }

        @Override
        public CompletableFuture<Optional<Tournament>> findById(UUID id) {
            return CompletableFuture.completedFuture(Optional.ofNullable(store.get(id)));
        }

        @Override
        public CompletableFuture<List<Tournament>> findAll() {
            return CompletableFuture.completedFuture(List.copyOf(store.values()));
        }

        @Override
        public CompletableFuture<Void> delete(UUID id) {
            store.remove(id);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Tournament> update(Tournament tournament) {
            store.put(tournament.id(), tournament);
            return CompletableFuture.completedFuture(tournament);
        }
    }
}
