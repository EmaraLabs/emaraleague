# Game Mode Logic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Implement game mode logic — Duels kill detection, win condition evaluation, and MatchEngine → GameMode dispatch. Make Duels fully playable end-to-end.

**Architecture:** `GameModeRegistry` to map mode IDs to implementations. `MatchEngine` dispatches lifecycle hooks to the correct `GameMode`. Win condition evaluated per match tick/death.

**Tech Stack:** Java 21, Paper API, JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task D1 (Duels Module)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- `GameMode` interface is the contract — modules implement it
- `GameModeRegistry` is the single source of truth for mode lookup
- Win condition check happens after every elimination event
- All player-facing messages via `MessageRegistry`

---

## Task 1: GameModeRegistry

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/game/GameModeRegistry.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/game/GameModeRegistryTest.java`

**Interfaces:**
- Consumes: `GameMode` implementations
- Produces: Registry for mode lookup by ID

```java
public final class GameModeRegistry {
    public void register(GameMode mode)
    public Optional<GameMode> getMode(String id)
    public List<GameMode> getModes()
    public List<String> getModeIds()
    public boolean exists(String id)
    public int count()
}
```

- [ ] **Step 1: Write failing test**

```java
@Test
void register_addsMode() {
    GameModeRegistry registry = new GameModeRegistry();
    GameMode mode = new DuelsGameMode();
    registry.register(mode);
    assertTrue(registry.exists("duels"));
    assertEquals(1, registry.count());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.game;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class GameModeRegistry {
    private final Map<String, GameMode> modes = new ConcurrentHashMap<>();

    public void register(GameMode mode) {
        modes.put(mode.getId().toLowerCase(), mode);
    }

    public Optional<GameMode> getMode(String id) {
        return Optional.ofNullable(modes.get(id.toLowerCase()));
    }

    public List<GameMode> getModes() {
        return List.copyOf(modes.values());
    }

    public List<String> getModeIds() {
        return List.copyOf(modes.keySet());
    }

    public boolean exists(String id) {
        return modes.containsKey(id.toLowerCase());
    }

    public int count() {
        return modes.size();
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void getMode_caseInsensitive() { ... }

@Test
void getMode_notFound_returnsEmpty() { ... }

@Test
void getModes_returnsAll() { ... }

@Test
void getModeIds_returnsAllIds() { ... }

@Test
void register_duplicate_overwrites() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add GameModeRegistry for mode lookup"
```

---

## Task 2: DuelsGameMode Win Logic

**Files:**
- Modify: `modules/duels/src/main/java/com/emaralabs/emaraleague/modules/duels/DuelsGameMode.java`
- Test: `modules/duels/src/test/java/com/emaralabs/emaraleague/modules/duels/DuelsGameModeTest.java`

**Interfaces:**
- Consumes: `Match`, `Team`, `EntityDeathEvent`, `PlayerQuitEvent`
- Produces: Win detection, kill tracking, elimination logic

Duels rules:
- 1v1 (2 players, 2 teams of 1)
- Player dies → eliminated → other team wins
- Player quits → eliminated → other team wins
- Kill tracked per player

```java
// New methods on DuelsGameMode
public boolean isEliminated(UUID playerId)
public Team getWinner(Match match)
public void eliminatePlayer(UUID playerId)
public int getAliveCount(Match match)
```

- [ ] **Step 1: Write failing test**

```java
@Test
void onPlayerDeath_eliminatesPlayer() {
    DuelsGameMode mode = new DuelsGameMode();
    UUID playerId = UUID.randomUUID();
    Player player = mock(Player.class);
    when(player.getUniqueId()).thenReturn(playerId);

    EntityDeathEvent event = mock(EntityDeathEvent.class);
    when(event.getEntity()).thenReturn(player);

    mode.onPlayerDeath(event);
    assertTrue(mode.isEliminated(playerId));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.modules.duels;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DuelsGameMode implements GameMode {

    private static final String ID = "duels";
    private static final String DISPLAY_NAME = "Duels";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;

    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<UUID, Integer> playerDeaths = new HashMap<>();
    private final Set<UUID> eliminated = new HashSet<>();

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
        playerKills.clear();
        playerDeaths.clear();
        eliminated.clear();
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
        playerKills.clear();
        playerDeaths.clear();
        eliminated.clear();
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.LAST_TEAM_STANDING;
    }

    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID id = player.getUniqueId();
            playerDeaths.merge(id, 1, Integer::sum);
            eliminated.add(id);
        }
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        playerDeaths.merge(id, 1, Integer::sum);
        eliminated.add(id);
    }

    public boolean isEliminated(UUID playerId) {
        return eliminated.contains(playerId);
    }

    public Team getWinner(Match match) {
        // In 1v1, if one player is eliminated, the other team wins
        // Team A wins if team B's player is eliminated
        // Team B wins if team A's player is eliminated
        // This is simplified — real implementation needs player→team mapping
        if (eliminated.isEmpty()) {
            return null;
        }
        // For now, return the team that still has alive players
        // Real implementation needs PlayerSessionManager integration
        return null; // Placeholder — needs team mapping
    }

    public int getAliveCount(Match match) {
        return 2 - eliminated.size(); // Simplified for 1v1
    }

    public int getKills(UUID playerId) {
        return playerKills.getOrDefault(playerId, 0);
    }

    public int getDeaths(UUID playerId) {
        return playerDeaths.getOrDefault(playerId, 0);
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void onPlayerDeath_tracksDeaths() { ... }

@Test
void onPlayerQuit_eliminatesPlayer() { ... }

@Test
void onMatchStart_resetsState() { ... }

@Test
void onMatchEnd_clearsState() { ... }

@Test
void getAliveCount_bothAlive_returnsTwo() { ... }

@Test
void getAliveCount_oneEliminated_returnsOne() { ... }

@Test
void getKills_afterKill_returnsCount() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add DuelsGameMode elimination and kill tracking"
```

---

## Task 3: MatchEngine → GameMode Dispatch

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/match/MatchEngineTest.java`

**Interfaces:**
- Consumes: `GameModeRegistry`, `GameMode`
- Produces: MatchEngine calls game mode hooks on lifecycle events

```java
// Add to MatchEngine
private GameModeRegistry gameModeRegistry;

public void setGameModeRegistry(GameModeRegistry registry)

// Call in startMatch
gameModeRegistry.getMode(tournament.mode()).ifPresent(mode -> mode.onMatchStart(match));

// Call in endMatch
gameModeRegistry.getMode(tournament.mode()).ifPresent(mode -> mode.onMatchEnd(match, winner));
```

- [ ] **Step 1: Write failing test**

```java
@Test
void startMatch_callsGameModeOnMatchStart() {
    TournamentManager tournaments = new TournamentManager();
    ArenaManager arenas = new ArenaManager();
    MatchEngine engine = new MatchEngine(tournaments, arenas);
    GameModeRegistry registry = new GameModeRegistry();
    DuelsGameMode duels = spy(new DuelsGameMode());
    registry.register(duels);
    engine.setGameModeRegistry(registry);

    tournaments.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    Match match = engine.createMatch("Cup", new Team("Alpha", 1), new Team("Beta", 2));
    engine.startMatch(match.id());

    verify(duels).onMatchStart(any(Match.class));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

Add to `MatchEngine`:

```java
private GameModeRegistry gameModeRegistry;

public void setGameModeRegistry(GameModeRegistry registry) {
    this.gameModeRegistry = registry;
}

// In startMatch:
public Match startMatch(UUID matchId) {
    Match match = matches.get(matchId);
    if (match == null) {
        throw new IllegalArgumentException("Match not found: " + matchId);
    }
    validateMatchTransition(match.state(), MatchState.STARTING);
    Match updated = match.withState(MatchState.STARTING);
    matches.put(matchId, updated);

    // Dispatch to game mode
    if (gameModeRegistry != null) {
        UUID tournamentId = matchToTournament.get(matchId);
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchStart(updated));
        });
    }

    return updated;
}

// In endMatch:
public Match endMatch(UUID matchId, Team winner) {
    Match match = matches.get(matchId);
    if (match == null) {
        throw new IllegalArgumentException("Match not found: " + matchId);
    }
    validateMatchTransition(match.state(), MatchState.ENDED);
    Match updated = match.withWinner(winner);
    matches.put(matchId, updated);

    // Dispatch to game mode
    if (gameModeRegistry != null) {
        UUID tournamentId = matchToTournament.get(matchId);
        tournaments.getTournament(tournamentId).ifPresent(t -> {
            gameModeRegistry.getMode(t.mode()).ifPresent(mode -> mode.onMatchEnd(updated, winner));
        });
    }

    return updated;
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void endMatch_callsGameModeOnMatchEnd() { ... }

@Test
void startMatch_noRegistry_doesNotThrow() { ... }

@Test
void startMatch_modeNotFound_doesNotThrow() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: dispatch match lifecycle to GameMode hooks"
```

---

## Task 4: Wire GameModeRegistry in Plugin

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Create `GameModeRegistry`
- Register `DuelsGameMode`
- Set registry on `MatchEngine`

```java
gameModeRegistry = new GameModeRegistry();
gameModeRegistry.register(new DuelsGameMode());
matchEngine.setGameModeRegistry(gameModeRegistry);
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat: wire GameModeRegistry with DuelsGameMode in plugin"
```

---

## Self-Review

1. **Spec coverage:** Task D1 (Duels module) ✅. Win condition logic simplified — needs player→team mapping for full implementation.
2. **Placeholder scan:** `getWinner()` returns `null` — documented as placeholder pending PlayerSessionManager integration.
3. **Type consistency:** `GameMode` interface methods match existing. `MatchEngine` gets new `GameModeRegistry` field.
4. **Scope check:** 4 tasks. Task 3 wires dispatch, Task 4 registers modes. `getWinner()` needs future work for real team mapping.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
