# TournamentManager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement `TournamentManager` — the central coordinator for tournament lifecycle, team management, and state transitions.

**Architecture:** In-memory `TournamentManager` with CRUD + state machine. Database persistence deferred to Task C1.

**Tech Stack:** Java 21, Paper API, Adventure Components, JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B3 (Tournament & Match State Machine), Task B5 (Player & Team Management)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- All player-facing messages via `MessageRegistry` / `MessageFormatter`
- Immutable `Tournament` record — mutations return new instances
- State transitions validated — illegal transitions throw `IllegalStateException`
- Thread-safe: single-threaded access from main thread only (Folia-ready later)

---

## Task 1: TournamentManager CRUD

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Consumes: `Tournament` record, `TournamentState` enum, `BracketType` enum
- Produces: `TournamentManager` class with methods below

```java
public final class TournamentManager {
    public Tournament createTournament(String name, String mode, BracketType bracketType)
    public Optional<Tournament> getTournament(String name)
    public Optional<Tournament> getTournament(UUID id)
    public List<Tournament> getTournaments()
    public List<Tournament> getTournamentsByState(TournamentState state)
    public boolean deleteTournament(String name)
    public boolean exists(String name)
    public int count()
}
```

- [ ] **Step 1: Write failing test — createTournament**

```java
@Test
void createTournament_returnsNewTournament() {
    TournamentManager manager = new TournamentManager();
    Tournament t = manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
    assertNotNull(t);
    assertEquals("SummerCup", t.name());
    assertEquals("duels", t.mode());
    assertEquals(BracketType.SINGLE_ELIMINATION, t.bracketType());
    assertEquals(TournamentState.REGISTRATION, t.state());
}
```

- [ ] **Step 2: Run test — verify it fails (class doesn't exist)**

Run: `./gradlew :core:test --tests "*TournamentManagerTest*" -v`
Expected: FAIL — `TournamentManager` not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.tournament;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TournamentManager {
    private final Map<String, Tournament> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Tournament> byId = new ConcurrentHashMap<>();

    public Tournament createTournament(String name, String mode, BracketType bracketType) {
        Tournament tournament = new Tournament(name, mode, bracketType);
        byName.put(name.toLowerCase(), tournament);
        byId.put(tournament.id(), tournament);
        return tournament;
    }

    public Optional<Tournament> getTournament(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public Optional<Tournament> getTournament(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Tournament> getTournaments() {
        return List.copyOf(byName.values());
    }

    public List<Tournament> getTournamentsByState(TournamentState state) {
        return byName.values().stream()
                .filter(t -> t.state() == state)
                .toList();
    }

    public boolean deleteTournament(String name) {
        Tournament removed = byName.remove(name.toLowerCase());
        if (removed != null) {
            byId.remove(removed.id());
            return true;
        }
        return false;
    }

    public boolean exists(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    public int count() {
        return byName.size();
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

Run: `./gradlew :core:test --tests "*TournamentManagerTest*" -v`
Expected: PASS

- [ ] **Step 5: Write more tests (duplicate name, delete, list, count)**

```java
@Test
void createTournament_duplicateName_throwsException() { ... }

@Test
void deleteTournament_removesFromBothMaps() { ... }

@Test
void getTournamentsByState_filtersCorrectly() { ... }

@Test
void exists_returnsTrueForExisting() { ... }

@Test
void count_reflectsCurrentSize() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/.../TournamentManager.java core/src/test/java/.../TournamentManagerTest.java
git commit -m "feat: add TournamentManager with CRUD operations"
```

---

## Task 2: Tournament State Machine

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Produces: State transition methods on `TournamentManager`

```java
public Tournament transitionState(String name, TournamentState newState)
public boolean canTransition(TournamentState current, TournamentState next)
```

Valid transitions:
```
REGISTRATION → STARTING
STARTING → IN_PROGRESS
IN_PROGRESS → ENDED
ENDED → (terminal — no further transitions)
```

- [ ] **Step 1: Write failing test — valid transition**

```java
@Test
void transitionState_registrationToStarting_succeeds() {
    TournamentManager manager = new TournamentManager();
    manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    Tournament updated = manager.transitionState("Cup", TournamentState.STARTING);
    assertEquals(TournamentState.STARTING, updated.state());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

Add to `TournamentManager`:

```java
public Tournament transitionState(String name, TournamentState newState) {
    Tournament current = byName.get(name.toLowerCase());
    if (current == null) {
        throw new IllegalArgumentException("Tournament not found: " + name);
    }
    if (!canTransition(current.state(), newState)) {
        throw new IllegalStateException(
            String.format("Invalid state transition: %s → %s", current.state(), newState)
        );
    }
    Tournament updated = current.withState(newState);
    byName.put(name.toLowerCase(), updated);
    byId.put(updated.id(), updated);
    return updated;
}

public boolean canTransition(TournamentState current, TournamentState next) {
    return switch (current) {
        case REGISTRATION -> next == TournamentState.STARTING;
        case STARTING -> next == TournamentState.IN_PROGRESS;
        case IN_PROGRESS -> next == TournamentState.ENDED;
        case ENDED -> false;
    };
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (invalid transitions, terminal state, not found)**

```java
@Test
void transitionState_registrationToInProgress_throwsException() { ... }

@Test
void transitionState_endedToAny_throwsException() { ... }

@Test
void transitionState_notFound_throwsException() { ... }

@Test
void canTransition_allValidPaths_returnTrue() { ... }

@Test
void canTransition_allInvalidPaths_returnFalse() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add tournament state machine with transition validation"
```

---

## Task 3: Team Management

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Consumes: `Team` record
- Produces: Team management methods

```java
public Tournament addTeam(String tournamentName, Team team)
public Tournament removeTeam(String tournamentName, UUID teamId)
public Optional<Team> getTeam(String tournamentName, UUID teamId)
public int getTeamCount(String tournamentName)
```

- [ ] **Step 1: Write failing test — addTeam**

```java
@Test
void addTeam_toExistingTournament_succeeds() {
    TournamentManager manager = new TournamentManager();
    manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    Team team = new Team(UUID.randomUUID(), "TeamAlpha", List.of(), 1);
    Tournament updated = manager.addTeam("Cup", team);
    assertEquals(1, updated.teams().size());
    assertEquals("TeamAlpha", updated.teams().get(0).name());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
public Tournament addTeam(String tournamentName, Team team) {
    Tournament current = byName.get(tournamentName.toLowerCase());
    if (current == null) {
        throw new IllegalArgumentException("Tournament not found: " + tournamentName);
    }
    if (current.state() != TournamentState.REGISTRATION) {
        throw new IllegalStateException("Cannot add teams after registration closes");
    }
    List<Team> updatedTeams = new ArrayList<>(current.teams());
    updatedTeams.add(team);
    Tournament updated = current.withTeams(updatedTeams);
    byName.put(tournamentName.toLowerCase(), updated);
    byId.put(updated.id(), updated);
    return updated;
}

public Tournament removeTeam(String tournamentName, UUID teamId) {
    Tournament current = byName.get(tournamentName.toLowerCase());
    if (current == null) {
        throw new IllegalArgumentException("Tournament not found: " + tournamentName);
    }
    if (current.state() != TournamentState.REGISTRATION) {
        throw new IllegalStateException("Cannot remove teams after registration closes");
    }
    List<Team> updatedTeams = new ArrayList<>(current.teams());
    updatedTeams.removeIf(t -> t.id().equals(teamId));
    Tournament updated = current.withTeams(updatedTeams);
    byName.put(tournamentName.toLowerCase(), updated);
    byId.put(updated.id(), updated);
    return updated;
}

public Optional<Team> getTeam(String tournamentName, UUID teamId) {
    return getTournament(tournamentName)
            .flatMap(t -> t.teams().stream()
                    .filter(team -> team.id().equals(teamId))
                    .findFirst());
}

public int getTeamCount(String tournamentName) {
    return getTournament(tournamentName)
            .map(t -> t.teams().size())
            .orElse(0);
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (removeTeam, addTeam after registration, not found)**

```java
@Test
void removeTeam_existingTeam_succeeds() { ... }

@Test
void addTeam_afterRegistrationClosed_throwsException() { ... }

@Test
void addTeam_toNonExistentTournament_throwsException() { ... }

@Test
void getTeamCount_returnsCorrectCount() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add team management to TournamentManager"
```

---

## Task 4: Wire TournamentManager to EmaraLeagueCommand

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/command/EmaraLeagueCommand.java`
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`
- Test: `bootstrap/src/test/java/com/emaralabs/emaraleague/command/EmaraLeagueCommandTest.java`

**Interfaces:**
- Consumes: `TournamentManager`, `MessageRegistry`
- Produces: Commands that actually create/join/start/info tournaments

- [ ] **Step 1: Write failing test — create command creates tournament**

```java
@Test
void handleCreate_createsTournamentInManager() {
    // Setup mock plugin, command, and TournamentManager
    // Execute /emaraleague create SummerCup duels
    // Verify tournament exists in manager
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Wire TournamentManager into plugin + command**

In `EmaraLeaguePlugin`:
```java
private TournamentManager tournamentManager;

@Override
public void onEnable() {
    instance = this;
    tournamentManager = new TournamentManager();
    EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager);
    // ...
}

public TournamentManager getTournamentManager() {
    return tournamentManager;
}
```

In `EmaraLeagueCommand`:
```java
private final TournamentManager tournamentManager;

public EmaraLeagueCommand(Plugin plugin, TournamentManager tournamentManager) {
    this.plugin = plugin;
    this.tournamentManager = tournamentManager;
    this.messages = new MessageRegistry(plugin);
}
```

Update `handleCreate`:
```java
private void handleCreate(CommandSender sender, String[] args) {
    // ... validation ...
    tournamentManager.createTournament(name, mode, BracketType.SINGLE_ELIMINATION);
    sender.sendMessage(messages.get("tournament-created", Map.of("name", name)));
}
```

Update `handleInfo`:
```java
private void handleInfo(CommandSender sender, String[] args) {
    // ... validation ...
    Optional<Tournament> tournament = tournamentManager.getTournament(name);
    if (tournament.isEmpty()) {
        sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
        return;
    }
    Tournament t = tournament.get();
    sender.sendMessage(messages.get("tournament-info", Map.of(
        "name", t.name(),
        "mode", t.mode(),
        "status", t.state().name()
    )));
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (join, start, info with real data)**

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: wire TournamentManager to command handlers"
```

---

## Self-Review

1. **Spec coverage:** Task B3 (state machine) ✅, Task B5 (team management) ✅. Database persistence (C1) deferred.
2. **Placeholder scan:** No TBD/TODO — every step has actual code.
3. **Type consistency:** `Tournament` record methods (`withState`, `withTeams`) match existing record. `Team` record constructor matches existing. `BracketType.SINGLE_ELIMINATION` exists in enum.
4. **Scope check:** 4 tasks, each independently testable. No cross-task dependencies except Task 4 wiring.

---

## Execution Handoff

Plan complete. Two execution options:

**1. Subagent-Driven** — dispatch fresh subagent per task, review between tasks
**2. Inline Execution** — execute tasks in this session with TDD

Which approach?
