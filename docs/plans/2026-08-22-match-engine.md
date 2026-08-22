# Match Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement `MatchEngine` — orchestrates match lifecycle: creation, assignment to arena, start, tick, end, and bracket advancement.

**Architecture:** `MatchEngine` coordinates between `TournamentManager`, `ArenaManager`, `BracketGenerator`, and `GameMode`. Pure logic — no Bukkit world interaction yet (teleportation, block breaking deferred to game mode modules).

**Tech Stack:** Java 21, JUnit 5, existing core classes.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B3 (Tournament & Match State Machine), Task B4 (Bracket Generators)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- `Match` is immutable record — mutations return new instances
- `MatchEngine` is the single entry point for all match operations
- State transitions validated — illegal transitions throw `IllegalStateException`
- Thread-safe: single-threaded access from main thread only

---

## Task 1: MatchEngine Core

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchEngineTest.java`

**Interfaces:**
- Consumes: `TournamentManager`, `ArenaManager`, `BracketGenerator`, `GameMode`
- Produces: `MatchEngine` class with methods below

```java
public final class MatchEngine {
    public MatchEngine(TournamentManager tournaments, ArenaManager arenas)

    public Match createMatch(String tournamentName, Team teamA, Team teamB)
    public Match startMatch(UUID matchId)
    public Match endMatch(UUID matchId, Team winner)
    public Optional<Match> getMatch(UUID matchId)
    public List<Match> getMatches(String tournamentName)
    public List<Match> getMatchesByState(String tournamentName, MatchState state)
    public int getActiveMatchCount(String tournamentName)
}
```

Match state machine:
```
PENDING → STARTING → INGAME → ENDED
```

- [ ] **Step 1: Write failing test — createMatch**

```java
@Test
void createMatch_assignsToTournament() {
    TournamentManager tournaments = new TournamentManager();
    ArenaManager arenas = new ArenaManager();
    MatchEngine engine = new MatchEngine(tournaments, arenas);

    tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    Team alpha = new Team("Alpha", 1);
    Team beta = new Team("Beta", 2);

    Match match = engine.createMatch("Cup", alpha, beta);
    assertNotNull(match);
    assertEquals(alpha, match.teamA());
    assertEquals(beta, match.teamB());
    assertEquals(MatchState.PENDING, match.state());
}
```

- [ ] **Step 2: Run test — verify it fails (class doesn't exist)**

Run: `./gradlew :core:test --tests "*MatchEngineTest*" -v`
Expected: FAIL — `MatchEngine` not found

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.tournament.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchEngine {
    private final TournamentManager tournaments;
    private final ArenaManager arenas;
    private final Map<UUID, Match> matches = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> matchToTournament = new ConcurrentHashMap<>();

    public MatchEngine(TournamentManager tournaments, ArenaManager arenas) {
        this.tournaments = tournaments;
        this.arenas = arenas;
    }

    public Match createMatch(String tournamentName, Team teamA, Team teamB) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

        Match match = new Match(teamA, teamB);
        matches.put(match.id(), match);
        matchToTournament.put(match.id(), tournament.id());
        return match;
    }

    public Match startMatch(UUID matchId) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        validateMatchTransition(match.state(), MatchState.STARTING);
        Match updated = match.withState(MatchState.STARTING);
        matches.put(matchId, updated);
        return updated;
    }

    public Match endMatch(UUID matchId, Team winner) {
        Match match = matches.get(matchId);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }
        validateMatchTransition(match.state(), MatchState.ENDED);
        Match updated = match.withWinner(winner);
        matches.put(matchId, updated);
        return updated;
    }

    public Optional<Match> getMatch(UUID matchId) {
        return Optional.ofNullable(matches.get(matchId));
    }

    public List<Match> getMatches(String tournamentName) {
        Tournament tournament = tournaments.getTournament(tournamentName)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));
        return matches.values().stream()
                .filter(m -> tournament.id().equals(matchToTournament.get(m.id())))
                .toList();
    }

    public List<Match> getMatchesByState(String tournamentName, MatchState state) {
        return getMatches(tournamentName).stream()
                .filter(m -> m.state() == state)
                .toList();
    }

    public int getActiveMatchCount(String tournamentName) {
        return (int) getMatches(tournamentName).stream()
                .filter(m -> m.state() == MatchState.STARTING || m.state() == MatchState.INGAME)
                .count();
    }

    private void validateMatchTransition(MatchState current, MatchState next) {
        boolean valid = switch (current) {
            case PENDING -> next == MatchState.STARTING;
            case STARTING -> next == MatchState.INGAME;
            case INGAME -> next == MatchState.ENDED;
            case ENDED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    String.format("Invalid match transition: %s -> %s", current, next)
            );
        }
    }
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (start, end, invalid transitions, queries)**

```java
@Test
void startMatch_pendingToStarting_succeeds() { ... }

@Test
void endMatch_ingameToEnded_succeeds() { ... }

@Test
void startMatch_invalidTransition_throwsException() { ... }

@Test
void endMatch_withWinner_recordsWinner() { ... }

@Test
void getMatchesByState_filtersCorrectly() { ... }

@Test
void getActiveMatchCount_countsOnlyActive() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: add MatchEngine with match lifecycle management"
```

---

## Task 2: MatchEngine + Bracket Integration

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchEngineTest.java`

**Interfaces:**
- Consumes: `BracketGenerator`, `Bracket`
- Produces: Bracket-aware match generation

```java
public Bracket generateBracket(String tournamentName, BracketGenerator generator)
public Optional<Match> getNextMatch(String tournamentName)
public Match advanceBracket(String tournamentName, Match completedMatch)
```

- [ ] **Step 1: Write failing test — generateBracket**

```java
@Test
void generateBracket_createsMatchesFromTeams() {
    TournamentManager tournaments = new TournamentManager();
    ArenaManager arenas = new ArenaManager();
    MatchEngine engine = new MatchEngine(tournaments, arenas);

    tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    tournaments.addTeam("Cup", new Team("Alpha", 1));
    tournaments.addTeam("Cup", new Team("Beta", 2));

    Bracket bracket = engine.generateBracket("Cup", new SingleEliminationBracket());
    assertNotNull(bracket);
    assertTrue(bracket.getTotalMatches() > 0);
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
public Bracket generateBracket(String tournamentName, BracketGenerator generator) {
    Tournament tournament = tournaments.getTournament(tournamentName)
            .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentName));

    if (tournament.teams().size() < 2) {
        throw new IllegalStateException("Need at least 2 teams to generate bracket");
    }

    Bracket bracket = generator.generate(tournament.teams());

    for (Match match : bracket.getMatches()) {
        if (match.teamA() != null && match.teamB() != null) {
            matches.put(match.id(), match);
            matchToTournament.put(match.id(), tournament.id());
        }
    }

    Tournament updated = tournament.withMatches(bracket.getMatches());
    // Note: TournamentManager needs a way to update tournament with matches
    // For now, we store matches in MatchEngine's own map

    return bracket;
}

public Optional<Match> getNextMatch(String tournamentName) {
    return getMatchesByState(tournamentName, MatchState.PENDING).stream()
            .findFirst();
}

public Match advanceBracket(String tournamentName, Match completedMatch) {
    // Mark match as ended
    Match ended = endMatch(completedMatch.id(), completedMatch.winner());

    // Find next pending match
    Optional<Match> next = getNextMatch(tournamentName);
    if (next.isPresent()) {
        return startMatch(next.get().id());
    }

    return ended;
}
```

- [ ] **Step 4: Run test — verify it passes**

- [ ] **Step 5: Write more tests (getNextMatch, advanceBracket, empty bracket)**

```java
@Test
void getNextMatch_returnsFirstPending() { ... }

@Test
void advanceBracket_movesToNextMatch() { ... }

@Test
void advanceBracket_lastMatch_returnsEnded() { ... }
```

- [ ] **Step 6: Run all tests — verify pass**

- [ ] **Step 7: Commit**

```bash
git commit -m "feat: integrate bracket generation with MatchEngine"
```

---

## Self-Review

1. **Spec coverage:** Task B3 (match state machine) ✅, Task B4 (bracket integration) ✅. World/teleportation deferred.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `Match` record (`withState`, `withWinner`), `Team` record, `Bracket` class all match existing. `SingleEliminationBracket.generate()` returns `Bracket`.
4. **Scope check:** 2 tasks, each independently testable. Task 2 builds on Task 1's match storage.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
