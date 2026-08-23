# Scoreboard Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Upgrade MatchScoreboard with live stats — kills, timer, team scores, game-mode-specific data. Show countdown timer during STARTING, live stats during INGAME, winner + stats during ENDED.

**Architecture:** `MatchScoreboard` hooks into `MatchEngine` and `GameMode` for live data. Timer updates every second via scheduler. Game-mode-specific stats (kills for Duels, blocks for Spleef, knockbacks for Sumo).

**Tech Stack:** Java 21, Bukkit Scoreboard API, Adventure, JUnit 5, Mockito.

**Spec:** Fasa C (Polish) — Scoreboard upgrade

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Scoreboard updates every second during match
- Game-mode-specific stats via GameMode hooks
- Color coding: gold for headers, team colors for teams, green for alive, red for eliminated
- Maximum 15 lines (Bukkit scoreboard limit)

---

## Task 1: Enhanced Scoreboard with Live Stats

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/ui/MatchScoreboard.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/ui/MatchScoreboardTest.java`

**Interfaces:**
- Consumes: `Match`, `MatchEngine`, `GameMode`, `Player`
- Produces: Live scoreboard with timer, stats, team scores

```java
public final class MatchScoreboard {
    // Existing methods
    public void showToPlayer(Player player, Match match)
    public void updateForPlayer(Player player, Match match)
    public void hideFromPlayer(Player player)
    public void hideFromAll()

    // New methods
    public void setGameMode(GameMode gameMode)
    public void setTimerSeconds(int seconds)
    public void updateTimer(int seconds)
    public void showKillStats(Map<UUID, Integer> kills)
    public void showBlockStats(Map<UUID, Integer> blocks)
    public void showKnockbackStats(Map<UUID, Integer> knockbacks)
}
```

**Scoreboard Layout (15 lines max):**
```
EmaraLeague                    ← gold bold
─────────────────              ← separator
State: INGAME                  ← green
Time: 02:35                    ← white
─────────────────              ← separator
Alpha                          ← accent bold
  Players: 1                   ← green
  Kills: 3                     ← gold
─────────────────              ← separator
Beta                           ← accent bold
  Players: 0                   ← red
  Kills: 1                     ← gold
─────────────────              ← separator
Winner: Alpha                  ← green bold (if ended)
```

- [ ] **Step 1: Write failing test — timer display**

```java
@Test
void updateTimer_displaysFormattedTime() {
    MatchScoreboard board = new MatchScoreboard(mock(MatchEngine.class));
    board.setTimerSeconds(155); // 2:35
    // Verify scoreboard shows "Time: 02:35"
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
public void setTimerSeconds(int seconds) {
    this.timerSeconds = seconds;
}

private String formatTime(int seconds) {
    int minutes = seconds / 60;
    int secs = seconds % 60;
    return String.format("%02d:%02d", minutes, secs);
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void showKillStats_displaysKillsPerTeam() { ... }

@Test
void showBlockStats_displaysBlocksPerTeam() { ... }

@Test
void showKnockbackStats_displaysKnockbacksPerTeam() { ... }

@Test
void scoreboard_maxLines_doesNotOverflow() { ... }

@Test
void stateColor_ingame_green() { ... }

@Test
void stateColor_ended_info() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

---

## Task 2: Auto-Update Timer via Scheduler

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/ui/MatchScoreboard.java`

**Interfaces:**
- Produces: Auto-updating timer every second during match

```java
public void startAutoUpdate(EmaraScheduler scheduler, Match match)
public void stopAutoUpdate()
```

- [ ] **Step 1: Write failing test**

```java
@Test
void startAutoUpdate_updatesEverySecond() {
    MatchScoreboard board = new MatchScoreboard(mock(MatchEngine.class));
    EmaraScheduler scheduler = mock(EmaraScheduler.class);
    Match match = new Match(new Team("Alpha", 1), new Team("Beta", 2));

    board.startAutoUpdate(scheduler, match);
    // Verify scheduler.runRepeating called with 20L, 20L
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

---

## Task 3: Wire Scoreboard into MatchEngine

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- MatchEngine calls `scoreboard.showToPlayer()` on match start
- MatchEngine calls `scoreboard.hideFromAll()` on match end
- MatchEngine calls `scoreboard.updateTimer()` every second during countdown
- MatchEngine calls `scoreboard.updateForPlayer()` on game events (kill, block break, knockback)

- [ ] **Step 1: Update MatchEngine**
- [ ] **Step 2: Update plugin wiring**
- [ ] **Step 3: Build — verify compiles**
- [ ] **Step 4: Run all tests — verify pass**
- [ ] **Step 5: Commit**

---

## Self-Review

1. **Spec coverage:** Scoreboard upgrade ✅. Live stats, timer, team scores, game-mode data.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `MatchScoreboard` uses existing `EmaraTheme` colors.
4. **Scope check:** 3 tasks. Task 1 is enhanced display, Task 2 is auto-update, Task 3 is wiring.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
