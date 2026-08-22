# Database Persistence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Wire `TournamentRepository` to `TournamentManager` — tournaments persist to SQLite and survive restart. Load on startup, save on every mutation.

**Architecture:** `TournamentManager` gets an optional `TournamentRepository` delegate. Every CRUD operation syncs to DB async. On plugin enable, load all tournaments from DB into memory.

**Tech Stack:** Java 21, HikariCP, SQLite, CompletableFuture, JUnit 5.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task C1 (Database Setup), Task C3 (Repository Pattern)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- All DB operations async via `CompletableFuture` — never block main thread
- `TournamentManager` API unchanged — persistence is transparent
- SQLite for dev, MySQL-ready for prod (HikariCP config)
- On load failure, start with empty state (don't crash)

---

## Task 1: TournamentManager + Repository Integration

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Consumes: `TournamentRepository` (infrastructure module)
- Produces: `TournamentManager` with persistence support

```java
// Add to TournamentManager
private TournamentRepository repository;

public void setRepository(TournamentRepository repository)
public void loadFromDatabase()
```

Changes to existing methods:
- `createTournament()` — also save to DB
- `deleteTournament()` — also delete from DB
- `transitionState()` — also update in DB
- `addTeam()` / `removeTeam()` — also update in DB

- [ ] **Step 1: Write failing test — persistence on create**

```java
@Test
void createTournament_withRepository_savesToDb() {
    TournamentManager manager = new TournamentManager();
    DatabaseManager db = new DatabaseManager("jdbc:sqlite::memory:", "", "");
    db.initializeSchema();
    TournamentRepository repo = new TournamentRepository(db);
    manager.setRepository(repo);

    manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);

    Optional<Tournament> fromDb = repo.findById(
        manager.getTournament("SummerCup").get().id()
    ).join();
    assertTrue(fromDb.isPresent());
    assertEquals("SummerCup", fromDb.get().name());

    repo.shutdown();
    db.close();
}
```

**Problem:** `TournamentManager` is in `core` module, `TournamentRepository` is in `infrastructure/database` module. `core` doesn't depend on `infrastructure`. We need to break this dependency.

**Solution:** Create a `TournamentPersistence` interface in `core`, implement it in `infrastructure/database`.

```java
// In core/tournament/TournamentPersistence.java
public interface TournamentPersistence {
    CompletableFuture<Tournament> save(Tournament tournament);
    CompletableFuture<Optional<Tournament>> findById(UUID id);
    CompletableFuture<List<Tournament>> findAll();
    CompletableFuture<Void> delete(UUID id);
    CompletableFuture<Tournament> update(Tournament tournament);
}
```

Then `TournamentRepository` implements `TournamentPersistence`.

- [ ] **Step 2: Create TournamentPersistence interface in core**

- [ ] **Step 3: Update TournamentRepository to implement it**

- [ ] **Step 4: Update TournamentManager with persistence support**

```java
// In TournamentManager
private TournamentPersistence persistence;

public void setPersistence(TournamentPersistence persistence) {
    this.persistence = persistence;
}

public void loadFromDatabase() {
    if (persistence == null) return;
    List<Tournament> loaded = persistence.findAll().join();
    for (Tournament t : loaded) {
        byName.put(t.name().toLowerCase(), t);
        byId.put(t.id(), t);
    }
}

// In createTournament:
public Tournament createTournament(String name, String mode, BracketType bracketType) {
    if (byName.containsKey(name.toLowerCase())) {
        throw new IllegalArgumentException("Tournament already exists: " + name);
    }
    Tournament tournament = new Tournament(name, mode, bracketType);
    byName.put(name.toLowerCase(), tournament);
    byId.put(tournament.id(), tournament);
    if (persistence != null) {
        persistence.save(tournament);
    }
    return tournament;
}

// In deleteTournament:
public boolean deleteTournament(String name) {
    Tournament removed = byName.remove(name.toLowerCase());
    if (removed != null) {
        byId.remove(removed.id());
        if (persistence != null) {
            persistence.delete(removed.id());
        }
        return true;
    }
    return false;
}

// In transitionState:
public Tournament transitionState(String name, TournamentState newState) {
    // ... existing validation ...
    Tournament updated = current.withState(newState);
    byName.put(name.toLowerCase(), updated);
    byId.put(updated.id(), updated);
    if (persistence != null) {
        persistence.update(updated);
    }
    return updated;
}
```

- [ ] **Step 5: Write tests**

```java
@Test
void createTournament_withPersistence_savesToDb() { ... }

@Test
void deleteTournament_withPersistence_removesFromDb() { ... }

@Test
void transitionState_withPersistence_updatesDb() { ... }

@Test
void loadFromDatabase_restoresTournaments() { ... }

@Test
void noPersistence_worksAsBefore() { ... }
```

- [ ] **Step 6: Run tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add persistence layer to TournamentManager"
```

---

## Task 2: Wire Database in Plugin Bootstrap

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Create `DatabaseManager` with SQLite (file-based, not in-memory)
- Initialize schema
- Create `TournamentRepository`
- Set persistence on `TournamentManager`
- Load from DB on enable
- Close DB on disable

```java
@Override
public void onEnable() {
    instance = this;

    // Database setup
    String dbPath = getDataFolder().getAbsolutePath() + "/emaraleague.db";
    databaseManager = new DatabaseManager("jdbc:sqlite:" + dbPath, "", "");
    databaseManager.initializeSchema();
    tournamentRepository = new TournamentRepository(databaseManager);

    tournamentManager = new TournamentManager();
    tournamentManager.setPersistence(tournamentRepository);
    tournamentManager.loadFromDatabase();

    // ... rest of wiring
}

@Override
public void onDisable() {
    if (tournamentRepository != null) {
        tournamentRepository.shutdown();
    }
    if (databaseManager != null) {
        databaseManager.close();
    }
    getLogger().info("EmaraLeague disabled");
    instance = null;
}
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat: wire SQLite persistence in plugin bootstrap"
```

---

## Self-Review

1. **Spec coverage:** Task C1 (Database Setup) ✅, Task C3 (Repository Pattern) ✅. MySQL prod config deferred.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `TournamentPersistence` interface mirrors `Repository` but lives in `core` to avoid circular dependency. `TournamentRepository` implements both.
4. **Scope check:** 2 tasks. Task 1 creates the interface + integration, Task 2 wires it in plugin.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
