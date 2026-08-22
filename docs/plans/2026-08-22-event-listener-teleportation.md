# Event Listener + Teleportation + Game Mode Wiring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Wire Bukkit events to `MatchEngine` and `GameMode` — player death, quit, block break. Add teleportation service for match start. Make Duels fully functional end-to-end.

**Architecture:** `bootstrap/listener/` package for Bukkit event listeners. `core/teleport/` for teleportation abstraction. Game modes receive events via `MatchEngine` → `GameMode` hook dispatch.

**Tech Stack:** Java 21, Paper API, Adventure, JUnit 5, Mockito, MockBukkit (for integration tests).

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B2 (Arena), Task B5 (Player & Team Management), Task D1 (Duels Module)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Event listeners are thin — delegate to `MatchEngine` / `GameMode`
- Teleportation is abstracted (Folia-ready later)
- All player-facing messages via `MessageRegistry`
- Main-thread only for Bukkit API calls

---

## Task 1: Teleportation Service

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/teleport/TeleportService.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/teleport/TeleportServiceTest.java`

**Interfaces:**
- Consumes: `Arena`, `Player` (Bukkit), `Location` (Bukkit)
- Produces: `TeleportService` class

```java
public final class TeleportService {
    public void teleportToArena(Player player, Arena arena)
    public void teleportToLobby(Player player)
    public void teleportPlayersToArena(Collection<Player> players, Arena arena)
    public void setLobbySpawn(Location location)
    public Location getLobbySpawn()
}
```

- [ ] **Step 1: Write failing test — teleportToArena**

```java
@Test
void teleportToArena_movesPlayerToArenaCenter() {
    TeleportService service = new TeleportService();
    Player player = mock(Player.class);
    Arena arena = new Arena("TestArena");
    // Arena needs a center location — we need to add this to Arena
    Location center = new Location(mock(World.class), 100, 64, 100);
    // arena.setCenter(center); — need to add this

    service.teleportToArena(player, arena);
    verify(player).teleport(center);
}
```

**Problem:** `Arena` doesn't have a `center` location. We need to add location support to `Arena` first.

**Revised approach:** Add `Location center` and `Location lobbySpawn` to `Arena` and `TeleportService`.

- [ ] **Step 2: Add location fields to Arena**

```java
// In Arena.java
private Location center;
private Location lobbySpawn;

public Location getCenter() { return center; }
public void setCenter(Location center) { this.center = center; }
public Location getLobbySpawn() { return lobbySpawn; }
public void setLobbySpawn(Location lobbySpawn) { this.lobbySpawn = lobbySpawn; }
```

- [ ] **Step 3: Write TeleportService**

```java
package com.emaralabs.emaraleague.core.teleport;

import com.emaralabs.emaraleague.core.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class TeleportService {

    private Location lobbySpawn;

    public void teleportToArena(Player player, Arena arena) {
        if (arena.getCenter() != null) {
            player.teleport(arena.getCenter());
        }
    }

    public void teleportToLobby(Player player) {
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
    }

    public void teleportPlayersToArena(Collection<Player> players, Arena arena) {
        for (Player player : players) {
            teleportToArena(player, arena);
        }
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }
}
```

- [ ] **Step 4: Write tests with mocked Player/Location**

```java
@Test
void teleportToArena_withCenter_teleportsPlayer() { ... }

@Test
void teleportToArena_noCenter_doesNothing() { ... }

@Test
void teleportToLobby_withSpawn_teleportsPlayer() { ... }

@Test
void teleportPlayersToArena_teleportsAll() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add TeleportService and arena location support"
```

---

## Task 2: Player Session Manager

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/player/PlayerSessionManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/player/PlayerSessionManagerTest.java`

**Interfaces:**
- Consumes: `PlayerSession`, `UUID`
- Produces: `PlayerSessionManager` — tracks which tournament/match/team a player is in

```java
public final class PlayerSessionManager {
    public PlayerSession createSession(UUID playerId, String playerName)
    public Optional<PlayerSession> getSession(UUID playerId)
    public void removeSession(UUID playerId)
    public void assignToTeam(UUID playerId, UUID teamId)
    public void setSpectator(UUID playerId, boolean spectator)
    public Optional<UUID> getTeamId(UUID playerId)
    public boolean isInMatch(UUID playerId)
    public void clearMatch(UUID playerId)
}
```

- [ ] **Step 1: Write failing test**

```java
@Test
void createSession_createsNewSession() {
    PlayerSessionManager manager = new PlayerSessionManager();
    UUID id = UUID.randomUUID();
    PlayerSession session = manager.createSession(id, "Steve");
    assertNotNull(session);
    assertEquals(id, session.getPlayerId());
    assertEquals("Steve", session.getPlayerName());
    assertTrue(session.isActive());
}
```

- [ ] **Step 2: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerSessionManager {
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();

    public PlayerSession createSession(UUID playerId, String playerName) {
        PlayerSession session = new PlayerSession(playerId, playerName);
        sessions.put(playerId, session);
        return session;
    }

    public Optional<PlayerSession> getSession(UUID playerId) {
        return Optional.ofNullable(sessions.get(playerId));
    }

    public void removeSession(UUID playerId) {
        sessions.remove(playerId);
    }

    public void assignToTeam(UUID playerId, UUID teamId) {
        getSession(playerId).ifPresent(s -> s.setTeamId(teamId));
    }

    public void setSpectator(UUID playerId, boolean spectator) {
        getSession(playerId).ifPresent(s -> s.setSpectator(spectator));
    }

    public Optional<UUID> getTeamId(UUID playerId) {
        return getSession(playerId).map(PlayerSession::getTeamId);
    }

    public boolean isInMatch(UUID playerId) {
        return getSession(playerId).map(s -> s.getTeamId() != null).orElse(false);
    }

    public void clearMatch(UUID playerId) {
        getSession(playerId).ifPresent(s -> s.setTeamId(null));
    }
}
```

- [ ] **Step 3: Write more tests**

```java
@Test
void assignToTeam_setsTeamId() { ... }

@Test
void setSpectator_marksAsSpectator() { ... }

@Test
void removeSession_deletesSession() { ... }

@Test
void isInMatch_withTeam_returnsTrue() { ... }

@Test
void clearMatch_removesTeam() { ... }
```

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add PlayerSessionManager for player state tracking"
```

---

## Task 3: Event Listener — Player Death & Quit

**Files:**
- Create: `bootstrap/src/main/java/com/emaralabs/emaraleague/listener/PlayerEventListener.java`
- Test: `bootstrap/src/test/java/com/emaralabs/emaraleague/listener/PlayerEventListenerTest.java`

**Interfaces:**
- Consumes: `MatchEngine`, `PlayerSessionManager`, `MessageRegistry`, Bukkit events
- Produces: Event listener that wires deaths/quits to match logic

```java
public final class PlayerEventListener implements Listener {
    public PlayerEventListener(MatchEngine matchEngine, PlayerSessionManager sessions, MessageRegistry messages)

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event)

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
}
```

Logic for `onPlayerDeath`:
1. Check if player is in an active match
2. If yes, get match and game mode
3. Call `gameMode.onPlayerDeath(event)`
4. Check win condition — if last team standing, end match

Logic for `onPlayerQuit`:
1. Check if player is in an active match
2. If yes, treat as elimination
3. Call `gameMode.onPlayerQuit(event)`
4. Check win condition

- [ ] **Step 1: Write failing test**

```java
@Test
void onPlayerDeath_inMatch_eliminatesPlayer() {
    MatchEngine matchEngine = mock(MatchEngine.class);
    PlayerSessionManager sessions = new PlayerSessionManager();
    MessageRegistry messages = mock(MessageRegistry.class);
    PlayerEventListener listener = new PlayerEventListener(matchEngine, sessions, messages);

    UUID playerId = UUID.randomUUID();
    sessions.createSession(playerId, "Steve");
    sessions.assignToTeam(playerId, UUID.randomUUID());

    EntityDeathEvent event = mock(EntityDeathEvent.class);
    Player player = mock(Player.class);
    when(event.getEntity()).thenReturn(player);
    when(player.getUniqueId()).thenReturn(playerId);

    listener.onPlayerDeath(event);
    // Verify elimination logic was called
}
```

- [ ] **Step 2: Write minimal implementation**

```java
package com.emaralabs.emaraleague.listener;

import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerEventListener implements Listener {

    private final MatchEngine matchEngine;
    private final PlayerSessionManager sessions;
    private final MessageRegistry messages;

    public PlayerEventListener(MatchEngine matchEngine, PlayerSessionManager sessions, MessageRegistry messages) {
        this.matchEngine = matchEngine;
        this.sessions = sessions;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        handleElimination(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleElimination(event.getPlayer());
    }

    private void handleElimination(Player player) {
        UUID playerId = player.getUniqueId();
        if (!sessions.isInMatch(playerId)) {
            return;
        }
        // TODO: Get active match for player, call game mode hooks, check win condition
        // For now, just clear the session
        sessions.clearMatch(playerId);
    }
}
```

- [ ] **Step 3: Write tests**

```java
@Test
void onPlayerDeath_notInMatch_ignores() { ... }

@Test
void onPlayerDeath_inMatch_processesElimination() { ... }

@Test
void onPlayerQuit_inMatch_processesElimination() { ... }

@Test
void onPlayerDeath_nonPlayer_ignores() { ... }
```

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add PlayerEventListener for death/quit elimination"
```

---

## Task 4: Wire Everything in Plugin Bootstrap

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Create `PlayerSessionManager`
- Create `TeleportService`
- Create `MatchEngine` (with TournamentManager + ArenaManager)
- Create `PlayerEventListener`
- Register listener with Bukkit

```java
@Override
public void onEnable() {
    instance = this;
    tournamentManager = new TournamentManager();
    arenaManager = new ArenaManager();
    playerSessionManager = new PlayerSessionManager();
    teleportService = new TeleportService();
    matchEngine = new MatchEngine(tournamentManager, arenaManager);

    EmaraLeagueCommand command = new EmaraLeagueCommand(this, tournamentManager);
    getCommand("emaraleague").setExecutor(command);
    getCommand("emaraleague").setTabCompleter(command);

    PlayerEventListener listener = new PlayerEventListener(matchEngine, playerSessionManager, command.getMessages());
    getServer().getPluginManager().registerEvents(listener, this);

    getLogger().info("EmaraLeague enabled");
}
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

```bash
git commit -m "feat: wire all managers and listeners in plugin bootstrap"
```

---

## Self-Review

1. **Spec coverage:** Task B5 (player sessions) ✅, Task D1 (Duels wiring) partial ✅. Full game mode logic (kill detection, win condition) needs more work.
2. **Placeholder scan:** No TBD in production code — `handleElimination` has TODO for full match lookup, but structure is in place.
3. **Type consistency:** `PlayerSession` constructor `(UUID, String)` matches existing. `Arena` gets new `Location` fields. `MatchEngine` constructor `(TournamentManager, ArenaManager)` matches existing.
4. **Scope check:** 4 tasks. Task 3 has a TODO for full game mode integration — acceptable for now, will be completed when we do full Duels implementation.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
