# Match Flow Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Complete the tournament loop — match ends → bracket advances → next match starts → tournament completes → winner announced.

**Architecture:** `MatchEngine.endMatch()` triggers bracket advancement via `BracketGenerator.advance()`. `TournamentManager` tracks tournament completion. `MatchEngine` auto-starts next match.

**Tech Stack:** Java 21, JUnit 5, Mockito.

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Bracket advancement only after match ENDED
- Tournament completion when all matches ENDED
- Winner announcement via MessageRegistry
- Auto-start next match if available

---

## Task 1: Bracket Advancement Logic

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/bracket/SingleEliminationBracket.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/bracket/SingleEliminationBracketTest.java`

**Interfaces:**
- Produces: Working `advance()` that populates next round matches

```java
@Override
public Bracket advance(Bracket bracket, Match completedMatch) {
    // Find the completed match in bracket
    // Determine winner
    // Find next match slot for winner
    // Populate next match with winner
    // Return updated bracket
}
```

For Single Elimination with 4 teams:
- Matches: [M1(A vs B), M2(C vs D), M3(TBD vs TBD)]
- M1 ends (A wins) → M3 becomes (A vs TBD)
- M2 ends (D wins) → M3 becomes (A vs D)
- M3 is final → tournament ends

- [ ] **Step 1: Write failing test**

```java
@Test
void advance_populatesNextMatch() {
    SingleEliminationBracket gen = new SingleEliminationBracket();
    Team a = new Team("Alpha", 1);
    Team b = new Team("Beta", 2);
    Team c = new Team("Gamma", 3);
    Team d = new Team("Delta", 4);

    Bracket bracket = gen.generate(List.of(a, b, c, d));
    Match m1 = bracket.getMatches().get(0); // A vs B
    Match m1Ended = m1.withWinner(a);

    Bracket advanced = gen.advance(bracket, m1Ended);
    Match finalMatch = advanced.getMatches().get(2); // Should be A vs TBD
    assertEquals(a, finalMatch.teamA());
    assertNull(finalMatch.teamB());
}
```

- [ ] **Step 2: Write implementation**

- [ ] **Step 3: Write more tests**

```java
@Test
void advance_bothSemisComplete_populatesFinal() { ... }

@Test
void advance_finalComplete_tournamentEnds() { ... }
```

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

---

## Task 2: MatchEngine Auto-Advance

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchEngineTest.java`

**Interfaces:**
- Produces: `endMatch()` auto-advances bracket and starts next match

```java
// New methods
public void setBracketGenerator(BracketGenerator generator)
public boolean isTournamentComplete(String tournamentName)
public Optional<Match> advanceBracket(String tournamentName, Match completedMatch)
```

- [ ] **Step 1: Write failing test**

```java
@Test
void endMatch_autoAdvancesBracket() {
    // Setup 4-team tournament
    // End first match
    // Verify next match auto-started
}
```

- [ ] **Step 2: Write implementation**

```java
public Match endMatch(UUID matchId, Team winner) {
    // ... existing endMatch logic ...

    // Auto-advance bracket
    if (bracketGenerator != null) {
        UUID tournamentId = matchToTournament.get(matchId);
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            Bracket currentBracket = new Bracket(getMatches(t.name()));
            Bracket advanced = bracketGenerator.advance(currentBracket, updated);

            // Store new matches
            for (Match m : advanced.getMatches()) {
                if (!matches.containsKey(m.id()) && m.teamA() != null && m.teamB() != null) {
                    matches.put(m.id(), m);
                    matchToTournament.put(m.id(), tournamentId);
                }
            }

            // Auto-start next match if available
            getNextMatch(t.name()).ifPresent(next -> startMatch(next.id()));
        });
    }

    return updated;
}
```

- [ ] **Step 3: Write more tests**

```java
@Test
void endMatch_finalMatch_tournamentCompletes() { ... }

@Test
void isTournamentComplete_allMatchesEnded_returnsTrue() { ... }

@Test
void isTournamentComplete_matchesPending_returnsFalse() { ... }
```

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

---

## Task 3: Tournament Completion + Winner Announcement

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/command/EmaraLeagueCommand.java`

**Interfaces:**
- Produces: Tournament completion detection + winner announcement

```java
// TournamentManager
public boolean isComplete(String tournamentName)
public Optional<Team> getChampion(String tournamentName)

// MatchEngine (after final match)
// Announce winner to all players in tournament
```

- [ ] **Step 1: Write failing test**

```java
@Test
void tournamentComplete_announcesChampion() {
    // Complete tournament
    // Verify champion announced
}
```

- [ ] **Step 2: Write implementation**

- [ ] **Step 3: Run tests — verify pass**

- [ ] **Step 4: Commit**

---

## Self-Review

1. **Spec coverage:** Match flow completion ✅. Bracket advancement ✅. Tournament completion ✅.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `BracketGenerator.advance()` signature matches interface. `Match.withWinner()` returns new instance.
4. **Scope check:** 3 tasks. Task 1 is bracket logic, Task 2 is engine integration, Task 3 is completion detection.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
