# Fasa A: Playable Sprint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Make EmaraLeague playable in-game — arena commands, teleportation, event wiring, scoreboard, config.

**Architecture:** 8 tasks that wire existing core logic to Bukkit/Paper runtime. After this sprint, server owners can create arenas, run tournaments, and players can actually play.

**Tech Stack:** Java 21, Paper API, Adventure, JUnit 5, Mockito.

---

## Task 1: Arena Commands

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/command/EmaraLeagueCommand.java`
- Test: `bootstrap/src/test/java/com/emaralabs/emaraleague/command/EmaraLeagueCommandTest.java`

**New commands:**
- `/emaraleague arena create <name>` — create arena
- `/emaraleague arena setcenter <name>` — set arena center to player location
- `/emaraleague arena setlobby <name>` — set lobby spawn to player location
- `/emaraleague arena list` — list all arenas
- `/emaraleague arena delete <name>` — delete arena

---

## Task 2: Match → Arena Assignment

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`

**Logic:**
- `startMatch()` auto-assigns an available arena (LOBBY state)
- Arena transitions: LOBBY → STARTING → INGAME
- On match end: arena transitions to ENDING → RESETTING → LOBBY

---

## Task 3: Player Teleport on Match Start/End

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`

**Logic:**
- On `startMatch()`: teleport all match players to arena center
- On `endMatch()`: teleport all match players back to lobby spawn

---

## Task 4: Fall Detection Listener

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/listener/PlayerEventListener.java`

**Logic:**
- Listen to `PlayerMoveEvent`
- If player Y < threshold (default 0), trigger `SpleefGameMode.onPlayerFall()`
- Then trigger win check

---

## Task 5: Block Break Listener Wiring

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/listener/PlayerEventListener.java`

**Logic:**
- Listen to `BlockBreakEvent`
- If player is in Spleef match, call `SpleefGameMode.onBlockBreak()`

---

## Task 6: Simple Scoreboard

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/ui/MatchScoreboard.java`

**Logic:**
- Show match info: teams, alive count, timer
- Update every second during match
- Use Adventure Scoreboard API

---

## Task 7: Config.yml

**Files:**
- Create: `bootstrap/src/main/resources/config.yml`
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/config/ConfigManager.java`

**Config keys:**
- `countdown-seconds: 10`
- `arena.fall-threshold: 0`
- `arena.auto-assign: true`

---

## Task 8: Build + Verify + Commit

- Full build, all tests pass
- Commit and push
- Verify CI green

---

## Execution Order

Tasks 1-7 can be done in parallel or sequence. I'll do them sequentially for clarity.
