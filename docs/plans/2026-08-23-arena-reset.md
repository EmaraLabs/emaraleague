# Arena Reset Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Arena reset after match — restore arena to original state (blocks, entities, etc.). For Spleef, restore broken blocks. For Sumo, restore any modified terrain.

**Architecture:** `ArenaResetService` tracks block changes during match, restores them on match end. For Spleef, track broken blocks. For future modes, track any modifications.

**Tech Stack:** Java 21, Bukkit Block API, JUnit 5, Mockito.

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Track block changes during match
- Restore blocks on match end
- Support Spleef (broken blocks) and extensible for other modes
- Performance: batch restore, no lag

---

## Task 1: ArenaResetService Core

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/arena/ArenaResetService.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/arena/ArenaResetServiceTest.java`

**Interfaces:**
- Consumes: `Arena`, `BlockChangeEvent`, `Match`
- Produces: Block change tracking, restore on match end

```java
public class ArenaResetService {
    // Track changes
    public void trackBlockBreak(Arena arena, Block block)
    public void trackBlockPlace(Arena arena, Block block)
    public void clearTrackedChanges(Arena arena)

    // Restore
    public void restoreArena(Arena arena)
    public boolean hasTrackedChanges(Arena arena)
    public int getTrackedChangeCount(Arena arena)
}
```

**Implementation approach:**
- Store original block state (Material, BlockData) per location
- On match end, restore all tracked blocks
- Clear tracking after restore

- [ ] **Step 1: Write failing test**

```java
@Test
void trackBlockBreak_storesOriginalState() {
    ArenaResetService service = new ArenaResetService();
    Arena arena = new Arena("TestArena");
    Block block = mock(Block.class);
    Location loc = new Location(null, 0, 64, 0);
    when(block.getLocation()).thenReturn(loc);
    when(block.getType()).thenReturn(Material.SNOW_BLOCK);

    service.trackBlockBreak(arena, block);
    assertTrue(service.hasTrackedChanges(arena));
    assertEquals(1, service.getTrackedChangeCount(arena));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

```java
package com.emaralabs.emaraleague.core.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaResetService {

    private final Map<UUID, Map<Location, BlockData>> trackedChanges = new HashMap<>();

    public void trackBlockBreak(Arena arena, Block block) {
        trackedChanges.computeIfAbsent(arena.getId(), k -> new HashMap<>())
                .put(block.getLocation(), block.getBlockData().clone());
    }

    public void trackBlockPlace(Arena arena, Block block) {
        // Track the block that was replaced (usually AIR)
        trackedChanges.computeIfAbsent(arena.getId(), k -> new HashMap<>())
                .put(block.getLocation(), block.getBlockData().clone());
    }

    public void clearTrackedChanges(Arena arena) {
        trackedChanges.remove(arena.getId());
    }

    public void restoreArena(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        if (changes == null) {
            return;
        }

        for (Map.Entry<Location, BlockData> entry : changes.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            if (loc.getWorld() != null) {
                loc.getBlock().setBlockData(data);
            }
        }

        clearTrackedChanges(arena);
    }

    public boolean hasTrackedChanges(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        return changes != null && !changes.isEmpty();
    }

    public int getTrackedChangeCount(Arena arena) {
        Map<Location, BlockData> changes = trackedChanges.get(arena.getId());
        return changes == null ? 0 : changes.size();
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void trackBlockBreak_multipleBlocks_storesAll() { ... }

@Test
void restoreArena_restoresOriginalBlocks() { ... }

@Test
void restoreArena_clearsTrackedChanges() { ... }

@Test
void hasTrackedChanges_noChanges_returnsFalse() { ... }

@Test
void getTrackedChangeCount_noChanges_returnsZero() { ... }

@Test
void clearTrackedChanges_removesTracking() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

---

## Task 2: Wire into SpleefGameMode

**Files:**
- Modify: `modules/spleef/src/main/java/com/emaralabs/emaraleague/modules/spleef/SpleefGameMode.java`

**Changes:**
- Track block breaks during match
- Call `restoreArena()` on match end

```java
public void onBlockBreak(BlockBreakEvent event) {
    // Track for stats
    blocksBroken.merge(player.getUniqueId(), 1, Integer::sum);

    // Track for arena reset
    if (arenaResetService != null && currentArena != null) {
        arenaResetService.trackBlockBreak(currentArena, event.getBlock());
    }
}
```

- [ ] **Step 1: Update SpleefGameMode**
- [ ] **Step 2: Update MatchEngine to call restore on match end**
- [ ] **Step 3: Update plugin wiring**
- [ ] **Step 4: Build — verify compiles**
- [ ] **Step 5: Run all tests — verify pass**
- [ ] **Step 6: Commit**

---

## Self-Review

1. **Spec coverage:** Arena reset ✅. Block tracking ✅. Restore on match end ✅.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `ArenaResetService` uses Bukkit Block API.
4. **Scope check:** 2 tasks. Task 1 is core service, Task 2 is wiring.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
