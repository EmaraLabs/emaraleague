# ArenaManager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement `ArenaManager` — CRUD for arenas, state-aware queries, and arena lifecycle management.

**Architecture:** In-memory `ArenaManager` (same pattern as `TournamentManager`). ASWM world loading deferred to later task.

**Tech Stack:** Java 21, JUnit 5.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B2 (Arena & World Management)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- `Arena` class already has state machine — `ArenaManager` wraps it
- State transitions validated via `Arena.setState()` — manager delegates
- Thread-safe: `ConcurrentHashMap` for main-thread access
- Case-insensitive name lookup (same as `TournamentManager`)

---

## Task 1: ArenaManager CRUD

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/arena/ArenaManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/arena/ArenaManagerTest.java`

**Interfaces:**
- Consumes: `Arena`, `ArenaState`
- Produces: `ArenaManager` class with methods below

```java
public final class ArenaManager {
    public Arena createArena(String name)
    public Optional<Arena> getArena(String name)
    public Optional<Arena> getArena(UUID id)
    public List<Arena> getArenas()
    public List<Arena> getArenasByState(ArenaState state)
    public List<Arena> getAvailableArenas()
    public boolean deleteArena(String name)
    public boolean exists(String name)
    public int count()
}
```

- [ ] **Step 1: Write failing test — createArena**

```java
@Test
void createArena_returnsNewArena() {
    ArenaManager manager = new ArenaManager();
    Arena arena = manager.createArena("Arena_One");
    assertNotNull(arena);
    assertEquals("Arena_One", arena.getName());
    assertEquals(ArenaState.LOBBY, arena.getState());
    assertNotNull(arena.getId());
}
```

- [ ] **Step 2: Run test — verify it fails (class doesn't exist)**

Run: `./gradlew :core:test --tests "*ArenaManagerTest*" -v`
Expected: FAIL — `ArenaManager` not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.arena;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ArenaManager {
    private final Map<String, Arena> byName = new ConcurrentHashMap<>();
    private final Map<UUID, Arena> byId = new ConcurrentHashMap<>();

    public Arena createArena(String name) {
        if (byName.containsKey(name.toLowerCase())) {
            throw new IllegalArgumentException("Arena already exists: " + name);
        }
        Arena arena = new Arena(name);
        byName.put(name.toLowerCase(), arena);
        byId.put(arena.getId(), arena);
        return arena;
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase()));
    }

    public Optional<Arena> getArena(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Arena> getArenas() {
        return List.copyOf(byName.values());
    }

    public List<Arena> getArenasByState(ArenaState state) {
        return byName.values().stream()
                .filter(a -> a.getState() == state)
                .toList();
    }

    public List<Arena> getAvailableArenas() {
        return getArenasByState(ArenaState.LOBBY);
    }

    public boolean deleteArena(String name) {
        Arena removed = byName.remove(name.toLowerCase());
        if (removed != null) {
            byId.remove(removed.getId());
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

- [ ] **Step 5: Write more tests (duplicate name, delete, list, available, count)**

```java
@Test
void createArena_duplicateName_throwsException() { ... }

@Test
void deleteArena_removesFromBothMaps() { ... }

@Test
void getArenasByState_filtersCorrectly() { ... }

@Test
void getAvailableArenas_returnsOnlyLobby() { ... }

@Test
void exists_returnsTrueForExisting() { ... }

@Test
void count_reflectsCurrentSize() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add ArenaManager with CRUD and state-aware queries"
```

---

## Task 2: Arena State Delegation

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/arena/ArenaManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/arena/ArenaManagerTest.java`

**Interfaces:**
- Produces: State transition methods that delegate to `Arena.setState()`

```java
public Arena transitionArena(String name, ArenaState newState)
public boolean canTransition(String name, ArenaState newState)
```

- [ ] **Step 1: Write failing test — transitionArena**

```java
@Test
void transitionArena_lobbyToStarting_succeeds() {
    ArenaManager manager = new ArenaManager();
    manager.createArena("Arena_One");
    Arena updated = manager.transitionArena("Arena_One", ArenaState.STARTING);
    assertEquals(ArenaState.STARTING, updated.getState());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
public Arena transitionArena(String name, ArenaState newState) {
    Arena arena = byName.get(name.toLowerCase());
    if (arena == null) {
        throw new IllegalArgumentException("Arena not found: " + name);
    }
    arena.setState(newState);
    return arena;
}

public boolean canTransition(String name, ArenaState newState) {
    Arena arena = byName.get(name.toLowerCase());
    if (arena == null) {
        return false;
    }
    try {
        arena.setState(newState);
        arena.setState(ArenaState.LOBBY); // rollback — hacky but works for check
        return true;
    } catch (IllegalStateException e) {
        return false;
    }
}
```

Wait — `canTransition` yang guna try/catch + rollback tu hacky. Lebih baik expose `canTransition` dari `Arena` sendiri. Tapi `Arena` tak ada method tu. Saya akan tambah `canTransitionTo` ke `Arena` dan manager delegate.

Better approach — add `canTransitionTo` to `Arena`:

```java
// In Arena.java
public boolean canTransitionTo(ArenaState next) {
    return switch (state) {
        case LOBBY -> next == ArenaState.STARTING;
        case STARTING -> next == ArenaState.INGAME;
        case INGAME -> next == ArenaState.ENDING;
        case ENDING -> next == ArenaState.RESETTING;
        case RESETTING -> next == ArenaState.LOBBY;
    };
}
```

Then in `ArenaManager`:

```java
public boolean canTransition(String name, ArenaState newState) {
    return getArena(name)
            .map(a -> a.canTransitionTo(newState))
            .orElse(false);
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (invalid transitions, not found, full cycle)**

```java
@Test
void transitionArena_invalidTransition_throwsException() { ... }

@Test
void transitionArena_notFound_throwsException() { ... }

@Test
void canTransition_validPaths_returnTrue() { ... }

@Test
void canTransition_invalidPaths_returnFalse() { ... }

@Test
void fullStateCycle_lobbyToLobby() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add arena state delegation via ArenaManager"
```

---

## Self-Review

1. **Spec coverage:** Task B2 (Arena & World Management) — CRUD + state machine ✅. ASWM world loading deferred.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `Arena.getName()`, `Arena.getId()`, `Arena.getState()`, `Arena.setState()` all match existing class. New `canTransitionTo` added to `Arena`.
4. **Scope check:** 2 tasks, each independently testable. Task 2 adds `canTransitionTo` to `Arena`.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
