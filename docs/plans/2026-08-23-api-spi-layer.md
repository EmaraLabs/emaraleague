# API/SPI Layer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Create EmaraLeagueAPI interface + EmaraAddon SPI — expose core functionality for addons to hook into. Addons can register listeners, access managers, and extend functionality without modifying core.

**Architecture:** 
- `EmaraLeagueAPI` — public interface that addons consume
- `EmaraAddon` — SPI interface that addons implement
- `EmaraLeaguePlugin` — implements API, manages addon lifecycle

**Tech Stack:** Java 21, JUnit 5, Mockito.

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- API version tracking for compatibility
- Addons register/unregister cleanly
- Thread-safe addon management
- No circular dependencies

---

## Task 1: EmaraAddon SPI Interface

**Files:**
- Create: `api/src/main/java/com/emaralabs/emaraleague/api/EmaraAddon.java`
- Test: `api/src/test/java/com/emaralabs/emaraleague/api/EmaraAddonTest.java`

**Interfaces:**
- Produces: `EmaraAddon` interface for addons to implement

```java
public interface EmaraAddon {
    String getId();
    String getName();
    String getVersion();
    int getRequiredApiVersion();
    void onEnable(EmaraLeagueAPI api);
    void onDisable();
}
```

- [ ] **Step 1: Write failing test**

```java
@Test
void addon_hasRequiredMethods() {
    EmaraAddon addon = new EmaraAddon() {
        @Override public String getId() { return "test-addon"; }
        @Override public String getName() { return "Test Addon"; }
        @Override public String getVersion() { return "1.0.0"; }
        @Override public int getRequiredApiVersion() { return 1; }
        @Override public void onEnable(EmaraLeagueAPI api) { }
        @Override public void onDisable() { }
    };

    assertEquals("test-addon", addon.getId());
    assertEquals("Test Addon", addon.getName());
    assertEquals("1.0.0", addon.getVersion());
    assertEquals(1, addon.getRequiredApiVersion());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

---

## Task 2: EmaraLeagueAPI Interface

**Files:**
- Create: `api/src/main/java/com/emaralabs/emaraleague/api/EmaraLeagueAPI.java`
- Test: `api/src/test/java/com/emaralabs/emaraleague/api/EmaraLeagueAPITest.java`

**Interfaces:**
- Produces: `EmaraLeagueAPI` interface for core to implement

```java
public interface EmaraLeagueAPI {
    int getApiVersion();
    
    TournamentManager getTournamentManager();
    ArenaManager getArenaManager();
    MatchEngine getMatchEngine();
    GameModeRegistry getGameModeRegistry();
    PlayerSessionManager getPlayerSessionManager();
    
    void registerAddon(EmaraAddon addon);
    void unregisterAddon(String addonId);
    List<EmaraAddon> getAddons();
    boolean isAddonEnabled(String addonId);
    
    void registerGameMode(GameMode gameMode);
    void unregisterGameMode(String gameModeId);
    
    void broadcastToTournament(String tournamentName, Component message);
    void broadcastToMatch(UUID matchId, Component message);
}
```

- [ ] **Step 1: Write failing test**

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

---

## Task 3: EmaraLeaguePlugin implements API

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- Implement `EmaraLeagueAPI`
- Manage addon lifecycle (register, enable, disable)
- Expose API via `getAPI()`

```java
public final class EmaraLeaguePlugin extends JavaPlugin implements EmaraLeagueAPI {
    private static final int API_VERSION = 1;
    private final Map<String, EmaraAddon> addons = new ConcurrentHashMap<>();
    
    public EmaraLeagueAPI getAPI() {
        return this;
    }
    
    @Override
    public int getApiVersion() {
        return API_VERSION;
    }
    
    @Override
    public void registerAddon(EmaraAddon addon) {
        if (addon.getRequiredApiVersion() > API_VERSION) {
            throw new IllegalArgumentException(
                addon.getName() + " requires API v" + addon.getRequiredApiVersion() + 
                " but current is v" + API_VERSION
            );
        }
        addons.put(addon.getId(), addon);
        addon.onEnable(this);
    }
    
    @Override
    public void unregisterAddon(String addonId) {
        EmaraAddon addon = addons.remove(addonId);
        if (addon != null) {
            addon.onDisable();
        }
    }
}
```

- [ ] **Step 1: Update plugin class**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

---

## Task 4: Addon Lifecycle Management

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- `onDisable()` calls `unregisterAddon()` for all addons
- Proper cleanup order (addons first, then core)

- [ ] **Step 1: Update onDisable**
- [ ] **Step 2: Build — verify compiles**
- [ ] **Step 3: Run all tests — verify pass**
- [ ] **Step 4: Commit**

---

## Self-Review

1. **Spec coverage:** API/SPI layer ✅. Addon lifecycle ✅. Compatibility check ✅.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `EmaraLeagueAPI` and `EmaraAddon` interfaces match.
4. **Scope check:** 4 tasks. Task 1 is SPI, Task 2 is API, Task 3 is implementation, Task 4 is lifecycle.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
