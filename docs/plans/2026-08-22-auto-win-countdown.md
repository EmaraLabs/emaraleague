# Auto-Win Detection + Match Countdown Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Auto-end match when win condition is met (last team standing). Add 10-second countdown before match start with BossBar display.

**Architecture:** `WinConditionEvaluator` checks win condition after every elimination. `MatchCountdown` uses `EmaraScheduler` for tick-based countdown. `PlayerEventListener` triggers win check after elimination.

**Tech Stack:** Java 21, Paper API (BossBar), JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B3 (Match State Machine), Task D1 (Duels Module)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Win check happens AFTER every elimination event
- Countdown uses scheduler ticks (20 ticks = 1 second)
- BossBar shows countdown to all players in match
- All player-facing messages via `MessageRegistry`

---

## Task 1: WinConditionEvaluator

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/match/WinConditionEvaluator.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/WinConditionEvaluatorTest.java`

**Interfaces:**
- Consumes: `Match`, `GameMode`, `PlayerSessionManager`
- Produces: Win condition evaluation

```java
public final class WinConditionEvaluator {
    public WinConditionEvaluator(PlayerSessionManager sessions)

    public Optional<Team> evaluate(Match match, GameMode mode)
    public boolean isMatchOver(Match match, GameMode mode)
    public Team getWinningTeam(Match match, GameMode mode)
}
```

For Duels (LAST_TEAM_STANDING):
- 2 teams, 1 player each
- If one player eliminated → other team wins
- If both eliminated → draw (return empty)

- [ ] **Step 1: Write failing test**

```java
@Test
void evaluate_oneEliminated_returnsOtherTeam() {
    PlayerSessionManager sessions = new PlayerSessionManager();
    WinConditionEvaluator evaluator = new WinConditionEvaluator(sessions);

    UUID playerA = UUID.randomUUID();
    UUID playerB = UUID.randomUUID();
    Team teamA = new Team("Alpha", 1);
    Team teamB = new Team("Beta", 2);

    sessions.createSession(playerA, "Steve");
    sessions.assignToTeam(playerA, teamA.id());
    sessions.createSession(playerB, "Alex");
    sessions.assignToTeam(playerB, teamB.id());

    Match match = new Match(teamA, teamB);
    DuelsGameMode mode = new DuelsGameMode();

    // Eliminate player A
    mode.onPlayerDeath(createDeathEvent(playerA));

    Optional<Team> winner = evaluator.evaluate(match, mode);
    assertTrue(winner.isPresent());
    assertEquals(teamB, winner.get());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

import java.util.Optional;
import java.util.UUID;

public final class WinConditionEvaluator {

    private final PlayerSessionManager sessions;

    public WinConditionEvaluator(PlayerSessionManager sessions) {
        this.sessions = sessions;
    }

    public Optional<Team> evaluate(Match match, GameMode mode) {
        if (mode.getWinCondition() != com.emaralabs.emaraleague.core.game.WinCondition.LAST_TEAM_STANDING) {
            return Optional.empty();
        }

        Team teamA = match.teamA();
        Team teamB = match.teamB();

        boolean teamAAlive = hasAlivePlayers(teamA, match, mode);
        boolean teamBAlive = hasAlivePlayers(teamB, match, mode);

        if (teamAAlive && !teamBAlive) {
            return Optional.of(teamA);
        }
        if (!teamAAlive && teamBAlive) {
            return Optional.of(teamB);
        }
        return Optional.empty();
    }

    public boolean isMatchOver(Match match, GameMode mode) {
        return evaluate(match, mode).isPresent();
    }

    private boolean hasAlivePlayers(Team team, Match match, GameMode mode) {
        // Check if any player in this team is NOT eliminated
        // For Duels (1v1), each team has 1 player
        // We need to find the player in this team and check if eliminated
        // Simplified: use mode.getAliveCount() for now
        // Real implementation needs player→team mapping from PlayerSessionManager
        return mode.getAliveCount(match) > 0;
    }
}
```

Wait — `hasAlivePlayers` perlu semak team mana yang masih hidup. Untuk Duels 1v1, kita perlu map player → team. `PlayerSessionManager` ada `getTeamId()`. Kita perlu cari player ID untuk setiap team.

Masalah: `Team` record hanya ada `id`, `name`, `seed` — tiada player list. Kita perlu track player→team mapping dalam `PlayerSessionManager`.

Simplified approach untuk Duels:
- `getAliveCount()` return `2 - eliminated.size()`
- Kalau `aliveCount == 1` → satu team menang
- Kalau `aliveCount == 0` → draw
- Kalau `aliveCount == 2` → masih main

Tapi kita tak tahu team mana yang menang. Kita perlu check: eliminated player ada dalam team mana?

```java
private boolean hasAlivePlayers(Team team, Match match, GameMode mode, PlayerSessionManager sessions) {
    // Find all players in this team
    // For Duels, we need to check if the team's player is eliminated
    // Since Team doesn't have player list, we check via PlayerSessionManager
    // This is a simplified approach — we need player→team mapping
    
    // For now, use a different approach:
    // If match is 1v1 and aliveCount == 1, the non-eliminated player wins
    // We need to know which team that player belongs to
    
    // Better: DuelsGameMode should track which team each player belongs to
    // Add to DuelsGameMode: Map<UUID, UUID> playerToTeam
    return true; // Placeholder
}
```

Better approach — tambah `playerToTeam` mapping dalam `DuelsGameMode`:

```java
// In DuelsGameMode
private final Map<UUID, UUID> playerToTeam = new HashMap<>();

public void assignPlayerToTeam(UUID playerId, UUID teamId) {
    playerToTeam.put(playerId, teamId);
}

public Optional<UUID> getTeamForPlayer(UUID playerId) {
    return Optional.ofNullable(playerToTeam.get(playerId));
}
```

Then `WinConditionEvaluator` guna ini untuk tentukan team mana yang masih hidup.

- [ ] **Step 4: Update DuelsGameMode with player→team mapping**

- [ ] **Step 5: Write WinConditionEvaluator with proper team detection**

```java
public Optional<Team> evaluate(Match match, GameMode mode) {
    if (!(mode instanceof DuelsGameMode duels)) {
        return Optional.empty();
    }

    Team teamA = match.teamA();
    Team teamB = match.teamB();

    boolean teamAAlive = isTeamAlive(teamA, duels);
    boolean teamBAlive = isTeamAlive(teamB, duels);

    if (teamAAlive && !teamBAlive) {
        return Optional.of(teamA);
    }
    if (!teamAAlive && teamBAlive) {
        return Optional.of(teamB);
    }
    return Optional.empty();
}

private boolean isTeamAlive(Team team, DuelsGameMode duels) {
    // Check if any player in this team is NOT eliminated
    // We need to find players assigned to this team
    // Since DuelsGameMode tracks playerToTeam, we check there
    // For now, simplified: check if eliminated set contains any player from this team
    // We need DuelsGameMode to expose playerToTeam mapping
    return true; // Placeholder
}
```

Hmm, ini jadi complicated sebab kita perlu expose `playerToTeam` dari `DuelsGameMode`. Mari kita simplify — tambah method `isTeamEliminated(UUID teamId)` dalam `DuelsGameMode`:

```java
// In DuelsGameMode
public boolean isTeamEliminated(UUID teamId) {
    // Check if all players in this team are eliminated
    // We need to know which players belong to this team
    return playerToTeam.entrySet().stream()
            .filter(e -> e.getValue().equals(teamId))
            .allMatch(e -> eliminated.contains(e.getKey()));
}
```

Then `WinConditionEvaluator`:

```java
private boolean isTeamAlive(Team team, DuelsGameMode duels) {
    return !duels.isTeamEliminated(team.id());
}
```

- [ ] **Step 6: Write tests**

```java
@Test
void evaluate_bothAlive_returnsEmpty() { ... }

@Test
void evaluate_oneEliminated_returnsOtherTeam() { ... }

@Test
void evaluate_bothEliminated_returnsEmpty() { ... }

@Test
void isMatchOver_oneEliminated_returnsTrue() { ... }

@Test
void isMatchOver_bothAlive_returnsFalse() { ... }
```

- [ ] **Step 7: Run tests — verify pass**

- [ ] **Step 8: Commit**

```bash
git commit -m "feat: add WinConditionEvaluator for auto-win detection"
```

---

## Task 2: MatchCountdown

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchCountdown.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchCountdownTest.java`

**Interfaces:**
- Consumes: `EmaraScheduler`, `MessageRegistry`, `Match`
- Produces: Countdown timer with BossBar display

```java
public final class MatchCountdown {
    public MatchCountdown(EmaraScheduler scheduler, MessageRegistry messages)

    public void startCountdown(Match match, int seconds, Runnable onComplete)
    public void cancel()
    public boolean isRunning()
    public int getRemainingSeconds()
}
```

Countdown flow:
1. Show BossBar "Match starting in Xs"
2. Every second, update BossBar
3. At 0, hide BossBar, call `onComplete`

- [ ] **Step 1: Write failing test**

```java
@Test
void startCountdown_runsForSpecifiedSeconds() {
    EmaraScheduler scheduler = mock(EmaraScheduler.class);
    MessageRegistry messages = mock(MessageRegistry.class);
    MatchCountdown countdown = new MatchCountdown(scheduler, messages);

    AtomicInteger ticks = new AtomicInteger(0);
    doAnswer(inv -> {
        ticks.incrementAndGet();
        return null;
    }).when(scheduler).runRepeating(any(), anyLong(), anyLong());

    Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
    countdown.startCountdown(match, 10, () -> {});

    assertTrue(countdown.isRunning());
    assertEquals(10, countdown.getRemainingSeconds());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;

public final class MatchCountdown {

    private final EmaraScheduler scheduler;
    private final MessageRegistry messages;
    private int remainingSeconds;
    private boolean running;

    public MatchCountdown(EmaraScheduler scheduler, MessageRegistry messages) {
        this.scheduler = scheduler;
        this.messages = messages;
        this.running = false;
        this.remainingSeconds = 0;
    }

    public void startCountdown(Match match, int seconds, Runnable onComplete) {
        this.remainingSeconds = seconds;
        this.running = true;

        scheduler.runRepeating(() -> {
            if (remainingSeconds <= 0) {
                running = false;
                onComplete.run();
                return;
            }
            // Update BossBar here
            remainingSeconds--;
        }, 0, 20); // 20 ticks = 1 second
    }

    public void cancel() {
        running = false;
        remainingSeconds = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void cancel_stopsCountdown() { ... }

@Test
void isRunning_default_returnsFalse() { ... }

@Test
void getRemainingSeconds_afterStart_returnsCorrect() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add MatchCountdown with scheduler integration"
```

---

## Task 3: Wire Auto-Win + Countdown in PlayerEventListener

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/listener/PlayerEventListener.java`
- Test: `bootstrap/src/test/java/com/emaralabs/emaraleague/listener/PlayerEventListenerTest.java`

**Changes:**
- Add `WinConditionEvaluator` to listener
- After elimination, check win condition
- If match over, call `MatchEngine.endMatch()`

```java
private void handleElimination(Player player) {
    UUID playerId = player.getUniqueId();
    if (!sessions.isInMatch(playerId)) {
        return;
    }
    sessions.clearMatch(playerId);

    // Check win condition
    // Find active match for this player
    // Evaluate win condition
    // If over, end match
}
```

- [ ] **Step 1: Write failing test**

```java
@Test
void onPlayerDeath_triggersWinCheck() {
    // Setup: create match, assign players, start match
    // Eliminate one player
    // Verify endMatch was called
}
```

- [ ] **Step 2: Write minimal implementation**

- [ ] **Step 3: Run tests — verify pass**

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: wire auto-win detection to player elimination"
```

---

## Task 4: Wire Countdown in MatchEngine

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`

**Changes:**
- `startMatch` starts countdown instead of immediate transition
- After countdown completes, transition to `INGAME`

```java
public Match startMatch(UUID matchId) {
    // ... existing validation ...
    Match updated = match.withState(MatchState.STARTING);
    matches.put(matchId, updated);

    // Start countdown
    if (countdown != null) {
        countdown.startCountdown(updated, 10, () -> beginPlay(matchId));
    }

    return updated;
}
```

- [ ] **Step 1: Update MatchEngine**
- [ ] **Step 2: Run tests — verify pass**
- [ ] **Step 3: Commit**

```bash
git commit -m "feat: integrate countdown into match start flow"
```

---

## Self-Review

1. **Spec coverage:** Auto-win detection ✅, Match countdown ✅. BossBar display needs Paper API integration.
2. **Placeholder scan:** `hasAlivePlayers` needs `DuelsGameMode` player→team mapping — documented.
3. **Type consistency:** `WinConditionEvaluator` uses `PlayerSessionManager`. `MatchCountdown` uses `EmaraScheduler`.
4. **Scope check:** 4 tasks. Task 1 is the core logic, Task 2 is countdown, Tasks 3-4 wire everything.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
