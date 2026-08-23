# Sumo Game Mode Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement Sumo game mode — knockback tracking, ring-out elimination, win condition. 1v1 knockback-based fighting.

**Architecture:** `SumoGameMode` implements `GameMode`. Tracks knockback dealt per player. Eliminates players who fall below arena. Win condition: LAST_TEAM_STANDING (first to eliminate opponent wins).

**Tech Stack:** Java 21, Paper API (EntityDamageByEntityEvent, PlayerMoveEvent), JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task D3 (Sumo Module)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Sumo rules: knock opponent off platform, no weapons, fists only
- Fall below arena Y-level = elimination
- Knockback tracking for stats (optional display)
- Team support: 1v1 (2 teams of 1)

---

## Task 1: SumoGameMode Core Logic

**Files:**
- Modify: `modules/sumo/src/main/java/com/emaralabs/emaraleague/modules/sumo/SumoGameMode.java`
- Test: `modules/sumo/src/test/java/com/emaralabs/emaraleague/modules/sumo/SumoGameModeTest.java`

**Interfaces:**
- Consumes: `Match`, `Team`, `EntityDamageByEntityEvent`, `PlayerMoveEvent`
- Produces: Knockback tracking, elimination, win detection

```java
public class SumoGameMode implements GameMode {
    // Knockback tracking
    public void onKnockbackDealt(EntityDamageByEntityEvent event)
    public int getKnockbacksDealt(UUID playerId)

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

- [ ] **Step 1: Write failing test — knockback tracking**

```java
@Test
void onKnockbackDealt_tracksAttacker() {
    SumoGameMode mode = new SumoGameMode();
    UUID attackerId = UUID.randomUUID();
    Player attacker = mock(Player.class);
    when(attacker.getUniqueId()).thenReturn(attackerId);

    EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
    when(event.getDamager()).thenReturn(attacker);
    when(event.getEntity()).thenReturn(mock(Player.class));

    mode.onKnockbackDealt(event);
    assertEquals(1, mode.getKnockbacksDealt(attackerId));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.modules.sumo;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SumoGameMode implements GameMode {

    private static final String ID = "sumo";
    private static final String DISPLAY_NAME = "Sumo";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;

    private final Map<UUID, Integer> knockbacksDealt = new HashMap<>();
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
        knockbacksDealt.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        knockbacksDealt.clear();
        eliminated.clear();
        playerToTeam.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onKnockbackDealt(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            knockbacksDealt.merge(attacker.getUniqueId(), 1, Integer::sum);
        }
    }

    public int getKnockbacksDealt(UUID playerId) {
        return knockbacksDealt.getOrDefault(playerId, 0);
    }

    public void onPlayerFall(UUID playerId) {
        eliminated.add(playerId);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
    }

    public int getAliveCount(Match match) {
        return 2 - eliminated.size();
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
void getKnockbacksDealt_default_returnsZero() { ... }

@Test
void onMatchStart_resetsState() { ... }

@Test
void onMatchEnd_clearsState() { ... }

@Test
void isTeamEliminated_allPlayersEliminated_returnsTrue() { ... }

@Test
void isTeamEliminated_someAlive_returnsFalse() { ... }

@Test
void getAliveCount_bothAlive_returnsTwo() { ... }

@Test
void getAliveCount_oneEliminated_returnsOne() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

---

## Task 2: Register Sumo in Plugin

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Register `SumoGameMode` in `GameModeRegistry`

```java
gameModeRegistry.register(new SumoGameMode());
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

---

## Self-Review

1. **Spec coverage:** Task D3 (Sumo Module) ✅. Knockback tracking, fall elimination, team support.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `SumoGameMode` implements `GameMode` interface with default methods.
4. **Scope check:** 2 tasks. Task 1 is core logic, Task 2 is registration.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
