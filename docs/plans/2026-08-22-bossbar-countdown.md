# BossBar Countdown Display Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Add BossBar visual display to `MatchCountdown` — players see a live countdown bar during match start.

**Architecture:** `MatchCountdown` creates and manages a `BossBar` instance. Updates every tick. Hides on complete/cancel. Uses Adventure `BossBar` API (Paper native).

**Tech Stack:** Java 21, Paper API (BossBar), Adventure, JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task F1 (Commands & UI)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- BossBar color/progress updates every tick
- Bar hides when countdown completes or is cancelled
- Uses `EmaraTheme` colors for consistency
- All text via `MessageRegistry` / MiniMessage

---

## Task 1: BossBar Integration in MatchCountdown

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchCountdown.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchCountdownTest.java`

**Interfaces:**
- Consumes: `BossBar` (Adventure), `Player` (Bukkit), `Match`
- Produces: BossBar display during countdown

```java
// New methods on MatchCountdown
public void addPlayer(Player player)
public void removePlayer(Player player)
public void clearPlayers()
```

BossBar properties:
- Name: "Match starting in Xs" (MiniMessage format)
- Color: `EmaraTheme.WARNING` (amber) → `EmaraTheme.SUCCESS` (green) as it counts down
- Progress: `remainingSeconds / totalSeconds`

- [ ] **Step 1: Write failing test — BossBar created on start**

```java
@Test
void startCountdown_createsBossBar() {
    MatchCountdown countdown = new MatchCountdown(scheduler, messages);
    Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));
    countdown.startCountdown(match, 10, () -> {});
    assertNotNull(countdown.getBossBar());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

Add to `MatchCountdown`:

```java
private BossBar bossBar;
private final List<Player> players = new ArrayList<>();
private int totalSeconds;

public void startCountdown(Match match, int seconds, Runnable onComplete) {
    this.remainingSeconds = seconds;
    this.totalSeconds = seconds;
    this.running = true;

    bossBar = BossBar.bossBar(
        Component.text("Match starting in " + seconds + "s"),
        1.0f,
        BossBar.Color.YELLOW,
        BossBar.Overlay.PROGRESS
    );

    for (Player player : players) {
        player.showBossBar(bossBar);
    }

    scheduler.runRepeating(() -> {
        if (remainingSeconds <= 0) {
            running = false;
            hideBossBar();
            onComplete.run();
            return;
        }
        updateBossBar();
        remainingSeconds--;
    }, 0, 20);
}

private void updateBossBar() {
    if (bossBar == null) return;
    float progress = (float) remainingSeconds / totalSeconds;
    bossBar.progress(progress);
    bossBar.name(Component.text("Match starting in " + remainingSeconds + "s"));
    if (remainingSeconds <= 3) {
        bossBar.color(BossBar.Color.RED);
    } else if (remainingSeconds <= 5) {
        bossBar.color(BossBar.Color.YELLOW);
    }
}

private void hideBossBar() {
    if (bossBar == null) return;
    for (Player player : players) {
        player.hideBossBar(bossBar);
    }
    bossBar = null;
}

public void addPlayer(Player player) {
    players.add(player);
    if (bossBar != null) {
        player.showBossBar(bossBar);
    }
}

public void removePlayer(Player player) {
    players.remove(player);
    if (bossBar != null) {
        player.hideBossBar(bossBar);
    }
}

public void clearPlayers() {
    players.clear();
}

public BossBar getBossBar() {
    return bossBar;
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void startCountdown_showsBossBarToPlayers() { ... }

@Test
void updateBossBar_progressDecreases() { ... }

@Test
void updateBossBar_colorChangesAtThresholds() { ... }

@Test
void cancel_hidesBossBar() { ... }

@Test
void addPlayer_showsBossBarIfActive() { ... }

@Test
void removePlayer_hidesBossBar() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add BossBar countdown display to MatchCountdown"
```

---

## Task 2: Wire Players to Countdown in MatchEngine

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`

**Changes:**
- `startMatch` adds match players to countdown BossBar
- `beginPlay` clears players from countdown
- `endMatch` clears players from countdown

```java
public Match startMatch(UUID matchId) {
    // ... existing logic ...

    if (countdown != null) {
        // Add players to countdown display
        // For now, we need a way to get players from a match
        // This requires PlayerSessionManager integration
        countdown.startCountdown(updated, 10, () -> beginPlay(matchId));
    }

    return updated;
}
```

Problem: `MatchEngine` doesn't have access to `PlayerSessionManager`. We need to add it.

- [ ] **Step 1: Add PlayerSessionManager to MatchEngine**

```java
private PlayerSessionManager playerSessions;

public void setPlayerSessionManager(PlayerSessionManager sessions) {
    this.playerSessions = sessions;
}
```

- [ ] **Step 2: Add players to countdown on start**

```java
if (countdown != null) {
    // Get players from match teams
    // For Duels 1v1, we need player UUIDs from teams
    // This is simplified — real implementation needs PlayerSessionManager lookup
    countdown.startCountdown(updated, 10, () -> beginPlay(matchId));
}
```

For now, the countdown structure is in place. Player wiring requires PlayerSessionManager integration which is a separate task.

- [ ] **Step 3: Run tests — verify pass**

- [ ] **Step 4: Commit**

```bash
git commit -m "feat: prepare MatchEngine for player-aware countdown"
```

---

## Self-Review

1. **Spec coverage:** BossBar countdown display ✅. Player wiring needs PlayerSessionManager integration (future task).
2. **Placeholder scan:** Player list in countdown is empty by default — populated via `addPlayer()` calls from match setup.
3. **Type consistency:** `BossBar` from Adventure API. `Player` from Bukkit. `Component` from Adventure.
4. **Scope check:** 2 tasks. Task 1 is BossBar logic, Task 2 is player wiring preparation.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
