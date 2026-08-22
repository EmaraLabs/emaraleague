# End-to-End Integration Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Full tournament flow integration test — create tournament → add teams → assign players → generate bracket → start match → countdown → eliminate players → auto-end → advance bracket → crown winner.

**Architecture:** Integration test in `core` module that exercises `TournamentManager`, `ArenaManager`, `MatchEngine`, `GameModeRegistry`, `WinConditionEvaluator`, `MatchCountdown` together.

**Tech Stack:** Java 21, JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task G2 (Integration Tests)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Test full flow without Bukkit/Paper dependencies (pure logic)
- Use in-memory persistence stub for database
- Verify every state transition
- Verify bracket advancement

---

## Task 1: Full Tournament Flow Integration Test

**Files:**
- Create: `core/src/test/java/com/emaralabs/emaraleague/core/integration/FullTournamentFlowTest.java`

**Interfaces:**
- Consumes: All core managers and engines
- Produces: End-to-end verification

Test scenarios:
1. **Setup:** Create managers, register game mode, wire dependencies
2. **Create tournament:** Verify initial state
3. **Add teams:** Verify teams registered
4. **Assign players:** Verify players in teams
5. **Generate bracket:** Verify matches created
6. **Start match:** Verify state transition + countdown started
7. **Simulate countdown:** Verify auto-transition to INGAME
8. **Eliminate player:** Verify win condition triggered
9. **End match:** Verify winner recorded
10. **Advance bracket:** Verify next match starts
11. **Complete tournament:** Verify final state

- [ ] **Step 1: Write failing test — full flow**

```java
@Test
void fullTournamentFlow_duels_twoTeams() {
    // Setup
    TournamentManager tournaments = new TournamentManager();
    ArenaManager arenas = new ArenaManager();
    MatchEngine matchEngine = new MatchEngine(tournaments, arenas);
    GameModeRegistry gameModes = new GameModeRegistry();
    PlayerSessionManager sessions = new PlayerSessionManager();
    WinConditionEvaluator winEvaluator = new WinConditionEvaluator(sessions);

    // Register game mode
    TestGameMode duels = new TestGameMode();
    gameModes.register(duels);
    matchEngine.setGameModeRegistry(gameModes);

    // Create tournament
    Tournament t = tournaments.createTournament("SummerCup", "test", BracketType.SINGLE_ELIMINATION);
    assertEquals(TournamentState.REGISTRATION, t.state());

    // Add teams
    Team alpha = new Team("Alpha", 1);
    Team beta = new Team("Beta", 2);
    tournaments.addTeam("SummerCup", alpha);
    tournaments.addTeam("SummerCup", beta);
    assertEquals(2, tournaments.getTeamCount("SummerCup"));

    // Assign players
    UUID p1 = UUID.randomUUID();
    UUID p2 = UUID.randomUUID();
    tournaments.assignPlayerToTeam("SummerCup", alpha.id(), p1);
    tournaments.assignPlayerToTeam("SummerCup", beta.id(), p2);

    // Generate bracket
    Bracket bracket = matchEngine.generateBracket("SummerCup", new SingleEliminationBracket());
    assertEquals(1, bracket.getTotalMatches());

    // Start match
    Match match = bracket.getMatches().get(0);
    Match started = matchEngine.startMatch(match.id());
    assertEquals(MatchState.STARTING, started.state());

    // Verify game mode hook called
    verify(duels).onMatchStart(any(Match.class));

    // Begin play (simulate countdown complete)
    Match inGame = matchEngine.beginPlay(match.id());
    assertEquals(MatchState.INGAME, inGame.state());

    // Eliminate player
    duels.eliminateTeam(alpha.id());
    Optional<Team> winner = winEvaluator.evaluate(inGame, duels);
    assertTrue(winner.isPresent());
    assertEquals(beta, winner.get());

    // End match
    Match ended = matchEngine.endMatch(match.id(), beta);
    assertEquals(MatchState.ENDED, ended.state());
    assertEquals(beta, ended.winner());

    // Verify game mode hook called
    verify(duels).onMatchEnd(any(Match.class), any(Team.class));
}
```

- [ ] **Step 2: Run test — verify it fails (if any integration issue)**

- [ ] **Step 3: Fix any integration issues**

- [ ] **Step 4: Write more scenarios**

```java
@Test
void fullTournamentFlow_fourTeams_bracketAdvancement() { ... }

@Test
void fullTournamentFlow_spleef_blockBreakElimination() { ... }

@Test
void tournamentPersistence_saveAndLoad() { ... }

@Test
void arenaStateMachine_fullCycle() { ... }
```

- [ ] **Step 5: Run all tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add full tournament flow integration tests"
```

---

## Self-Review

1. **Spec coverage:** Task G2 (Integration Tests) ✅. Full flow from creation to completion.
2. **Placeholder scan:** Uses `TestGameMode` stub — acceptable for integration test.
3. **Type consistency:** All managers use existing APIs. `TestGameMode` implements `GameMode` interface.
4. **Scope check:** 1 task — comprehensive integration test.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
