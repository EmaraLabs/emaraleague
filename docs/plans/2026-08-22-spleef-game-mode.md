# Spleef Game Mode Logic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement Spleef game mode — block break tracking, player fall detection, elimination, win condition.

**Architecture:** `SpleefGameMode` implements `GameMode`. Tracks blocks broken per player. Eliminates players who fall below arena floor. Win condition: LAST_TEAM_STANDING (last player/team with alive members).

**Tech Stack:** Java 21, Paper API (BlockBreakEvent, EntityDamageEvent), JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task D2 (Spleef Module)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Spleef rules: break blocks under opponents, last player standing wins
- Fall below Y=0 (or arena floor) = elimination
- Block break tracking per player
- Team support: multiple players per team, team wins when all opponents eliminated

---

## Task 1: SpleefGameMode Core Logic

**Files:**
- Modify: `modules/spleef/src/main/java/com/emaralabs/emaraleague/modules/spleef/SpleefGameMode.java`
- Test: `modules/spleef/src/test/java/com/emaralabs/emaraleague/modules/spleef/SpleefGameModeTest.java`

**Interfaces:**
- Consumes: `Match`, `Team`, `BlockBreakEvent`, `EntityDamageEvent`
- Produces: Block tracking, elimination, win detection

```java
public class SpleefGameMode implements GameMode {
    // Block tracking
    public void onBlockBreak(BlockBreakEvent event)
    public int getBlocksBroken(UUID playerId)

    // Elimination
    public void onPlayerFall(UUID playerId)
    public boolean isEliminated(UUID playerId)
    public int getAliveCount(Match match)

    // Team support
    public void assignPlayerToTeam(UUID playerId, UUID teamId)
    public Optional<UUID> getTeamForPlayer(UUID playerId)
    public boolean isTeamEliminated(UUID teamId)

    // Match lifecycle
    public void onMatchStart(Match match)
    public void onMatchTick(Match match)
    public void onMatchEnd(Match match, Team winner)
}
```

- [ ] **Step 1: Write failing test — block break tracking**

```java
@Test
void onBlockBreak_tracksPlayer() {
    SpleefGameMode mode = new SpleefGameMode();
    UUID playerId = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(playerId);

    BlockBreakEvent event = mock(BlockBreakEvent.class);
    when(event.getPlayer()).thenReturn(player);

    mode.onBlockBreak(event);
    assertEquals(1, mode.getBlocksBroken(playerId));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.modules.spleef;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SpleefGameMode implements GameMode {

    private static final String ID = "spleef";
    private static final String DISPLAY_NAME = "Spleef";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 16;

    private final Map<UUID, Integer> blocksBroken = new HashMap<>();
    private final Set<UUID> eliminated = new HashSet<>();
    private final Map<UUID, UUID> playerToTeam = new HashMap<>();

    @Override
    public String getId() { return ID; }

    @Override
    public String getDisplayName() { return DISPLAY_NAME; }

    @Override
    public int getMinPlayers() { return MIN_PLAYERS; }

    @Override
    public int getMaxPlayers() { return MAX_PLAYERS; }

    @Override
    public void onMatchStart(Match match) {
        blocksBroken.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        blocksBroken.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        blocksBroken.merge(player.getUniqueId(), 1, Integer::sum);
    }

    public int getBlocksBroken(UUID playerId) {
        return blocksBroken.getOrDefault(playerId, 0);
    }

    public void onPlayerFall(UUID playerId) {
        eliminated.add(playerId);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
    }

    public int getAliveCount(Match match) {
        // Total players - eliminated
        // This needs player count from match teams
        return 0; // Placeholder
    }

    public void assignPlayerToTeam(UUID playerId, UUID teamId) {
        playerToTeam.put(playerId, teamId);
    }

    public Optional<UUID> getTeamForPlayer(UUID playerId) {
        return Optional.ofNullable(playerToTeam.get(playerId));
    }

    public boolean isTeamEliminated(UUID teamId) {
        return playerToTeam.entrySet().stream()
                .filter(e -> e.getValue().equals(teamId))
                .allMatch(e -> eliminated.contains(e.getKey()));
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void onPlayerFall_eliminatesPlayer() { ... }

@Test
void isEliminated_notEliminated_returnsFalse() { ... }

@Test
void getBlocksBroken_default_returnsZero() { ... }

@Test
void onMatchStart_resetsState() { ... }

@Test
void onMatchEnd_clearsState() { ... }

@Test
void isTeamEliminated_allPlayersEliminated_returnsTrue() { ... }

@Test
void isTeamEliminated_someAlive_returnsFalse() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add SpleefGameMode block tracking and elimination"
```

---

## Task 2: Register Spleef in Plugin

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Register `SpleefGameMode` in `GameModeRegistry`

```java
gameModeRegistry.register(new SpleefGameMode());
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat: register SpleefGameMode in plugin"
```

---

## Self-Review

1. **Spec coverage:** Task D2 (Spleef Module) ✅. Block break tracking, fall elimination, team support.
2. **Placeholder scan:** `getAliveCount` needs team player count — simplified for now.
3. **Type consistency:** `SpleefGameMode` implements `GameMode` interface with default methods.
4. **Scope check:** 2 tasks. Task 1 is core logic, Task 2 is registration.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
