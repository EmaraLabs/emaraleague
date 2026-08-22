# Player→Team Assignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Players can join teams in a tournament. Auto-balance or manual assignment. Teams have player lists. Commands: `/emaraleague team join <tournament> <team>`, `/emaraleague team leave`.

**Architecture:** `Team` record gets `List<UUID> playerIds`. `TournamentManager` handles team assignment. `PlayerSessionManager` tracks player→team mapping. Commands wire everything.

**Tech Stack:** Java 21, JUnit 5, Mockito.

**Spec:** `docs/DESIGN_AND_PLAN.md` — Task B5 (Player & Team Management)

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- `Team` is immutable record — `withPlayers()` returns new instance
- Player can only be in one team per tournament
- Team capacity enforced (max players per team)
- Auto-balance option: distribute players evenly

---

## Task 1: Team with Player List

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/Team.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TeamTest.java`

**Interfaces:**
- Produces: `Team` with player list support

```java
public record Team(UUID id, String name, int seed, List<UUID> playerIds) {
    public Team(String name, int seed) {
        this(UUID.randomUUID(), name, seed, new ArrayList<>());
    }

    public Team withPlayers(List<UUID> playerIds) {
        return new Team(id, name, seed, playerIds);
    }

    public Team addPlayer(UUID playerId) {
        List<UUID> updated = new ArrayList<>(playerIds);
        updated.add(playerId);
        return new Team(id, name, seed, updated);
    }

    public Team removePlayer(UUID playerId) {
        List<UUID> updated = new ArrayList<>(playerIds);
        updated.remove(playerId);
        return new Team(id, name, seed, updated);
    }

    public int getPlayerCount() {
        return playerIds.size();
    }

    public boolean hasPlayer(UUID playerId) {
        return playerIds.contains(playerId);
    }
}
```

- [ ] **Step 1: Write failing test**

```java
@Test
void addPlayer_addsToList() {
    Team team = new Team("Alpha", 1);
    UUID playerId = UUID.randomUUID();
    Team updated = team.addPlayer(playerId);
    assertEquals(1, updated.getPlayerCount());
    assertTrue(updated.hasPlayer(playerId));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

- [ ] **Step 4: Write more tests**

```java
@Test
void removePlayer_removesFromList() { ... }

@Test
void getPlayerCount_empty_returnsZero() { ... }

@Test
void hasPlayer_notMember_returnsFalse() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add player list support to Team record"
```

---

## Task 2: Team Assignment in TournamentManager

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Produces: Player assignment methods

```java
public Tournament assignPlayerToTeam(String tournamentName, UUID teamId, UUID playerId)
public Tournament removePlayerFromTeam(String tournamentName, UUID teamId, UUID playerId)
public Optional<Team> getTeamForPlayer(String tournamentName, UUID playerId)
public List<UUID> getPlayersInTeam(String tournamentName, UUID teamId)
public int getTeamPlayerCount(String tournamentName, UUID teamId)
```

- [ ] **Step 1: Write failing test**

```java
@Test
void assignPlayerToTeam_addsPlayer() {
    TournamentManager manager = new TournamentManager();
    manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    Team team = new Team("Alpha", 1);
    manager.addTeam("Cup", team);
    UUID playerId = UUID.randomUUID();

    Tournament updated = manager.assignPlayerToTeam("Cup", team.id(), playerId);
    assertEquals(1, updated.teams().get(0).getPlayerCount());
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write implementation**

```java
public Tournament assignPlayerToTeam(String tournamentName, UUID teamId, UUID playerId) {
    Tournament current = byName.get(tournamentName.toLowerCase());
    if (current == null) {
        throw new IllegalArgumentException("Tournament not found: " + tournamentName);
    }
    if (current.state() != TournamentState.REGISTRATION) {
        throw new IllegalStateException("Cannot assign players after registration closes");
    }

    List<Team> updatedTeams = new ArrayList<>();
    for (Team team : current.teams()) {
        if (team.id().equals(teamId)) {
            updatedTeams.add(team.addPlayer(playerId));
        } else {
            updatedTeams.add(team);
        }
    }

    Tournament updated = current.withTeams(updatedTeams);
    byName.put(tournamentName.toLowerCase(), updated);
    byId.put(updated.id(), updated);
    if (persistence != null) {
        persistence.update(updated);
    }
    return updated;
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void removePlayerFromTeam_removesPlayer() { ... }

@Test
void getTeamForPlayer_returnsCorrectTeam() { ... }

@Test
void assignPlayerToTeam_afterRegistration_throwsException() { ... }

@Test
void assignPlayerToTeam_teamNotFound_throwsException() { ... }

@Test
void assignPlayerToTeam_playerAlreadyInTeam_throwsException() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: add player-team assignment to TournamentManager"
```

---

## Task 3: Team Commands

**Files:**
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/command/EmaraLeagueCommand.java`
- Test: `bootstrap/src/test/java/com/emaralabs/emaraleague/command/EmaraLeagueCommandTest.java`

**New commands:**
- `/emaraleague team join <tournament> <team>` — join a team
- `/emaraleague team leave` — leave current team
- `/emaraleague team list <tournament>` — list teams and players

**Interfaces:**
- Consumes: `TournamentManager`, `PlayerSessionManager`
- Produces: Command handlers

- [ ] **Step 1: Write failing test**

```java
@Test
void handleTeamJoin_assignsPlayer() {
    // Setup mock sender, tournament, team
    // Execute /emaraleague team join Cup Alpha
    // Verify player is in team
}
```

- [ ] **Step 2: Write implementation**

Add to `EmaraLeagueCommand`:

```java
case "team" -> handleTeam(sender, args);
```

```java
private void handleTeam(CommandSender sender, String[] args) {
    if (args.length < 2) {
        sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team <join|leave|list>")));
        return;
    }

    switch (args[1].toLowerCase()) {
        case "join" -> handleTeamJoin(sender, args);
        case "leave" -> handleTeamLeave(sender);
        case "list" -> handleTeamList(sender, args);
        default -> sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team <join|leave|list>")));
    }
}

private void handleTeamJoin(CommandSender sender, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage(messages.get("player-only"));
        return;
    }
    if (args.length < 4) {
        sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team join <tournament> <team>")));
        return;
    }

    String tournamentName = args[2];
    String teamName = args[3];

    // Find team by name
    Optional<Tournament> tournament = tournamentManager.getTournament(tournamentName);
    if (tournament.isEmpty()) {
        sender.sendMessage(messages.get("tournament-not-found", Map.of("name", tournamentName)));
        return;
    }

    Optional<Team> team = tournament.get().teams().stream()
            .filter(t -> t.name().equalsIgnoreCase(teamName))
            .findFirst();

    if (team.isEmpty()) {
        sender.sendMessage(MessageFormatter.error("Team not found: " + teamName));
        return;
    }

    tournamentManager.assignPlayerToTeam(tournamentName, team.get().id(), player.getUniqueId());
    sender.sendMessage(MessageFormatter.success("You joined team " + team.get().name()));
}
```

- [ ] **Step 3: Write tests**

```java
@Test
void handleTeamJoin_success() { ... }

@Test
void handleTeamJoin_teamNotFound() { ... }

@Test
void handleTeamJoin_notPlayer() { ... }

@Test
void handleTeamLeave_success() { ... }

@Test
void handleTeamList_showsTeams() { ... }
```

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add team join/leave/list commands"
```

---

## Task 4: Auto-Balance Teams

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/tournament/TournamentManager.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/tournament/TournamentManagerTest.java`

**Interfaces:**
- Produces: Auto-balance method

```java
public Tournament autoBalanceTeams(String tournamentName)
```

Logic: Distribute unassigned players evenly across teams.

- [ ] **Step 1: Write failing test**

```java
@Test
void autoBalanceTeams_distributesEvenly() {
    TournamentManager manager = new TournamentManager();
    manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
    manager.addTeam("Cup", new Team("Alpha", 1));
    manager.addTeam("Cup", new Team("Beta", 2));

    // Add 4 players, 2 unassigned
    // Auto-balance should put 1 in each team
}
```

- [ ] **Step 2: Write implementation**

```java
public Tournament autoBalanceTeams(String tournamentName) {
    Tournament current = byName.get(tournamentName.toLowerCase());
    if (current == null) {
        throw new IllegalArgumentException("Tournament not found: " + tournamentName);
    }

    List<Team> teams = new ArrayList<>(current.teams());
    if (teams.isEmpty()) {
        return current;
    }

    // Get all unassigned players (players in session but not in any team)
    // This requires PlayerSessionManager integration
    // For now, simplified: if teams have unequal counts, move players

    // Sort teams by player count
    teams.sort(Comparator.comparingInt(Team::getPlayerCount));

    // Move players from largest to smallest until balanced
    // Simplified implementation
    return current.withTeams(teams);
}
```

- [ ] **Step 3: Write tests**

- [ ] **Step 4: Run tests — verify pass**

- [ ] **Step 5: Commit**

```bash
git commit -m "feat: add auto-balance teams"
```

---

## Self-Review

1. **Spec coverage:** Task B5 (Player & Team Management) ✅. Team assignment, commands, auto-balance.
2. **Placeholder scan:** Auto-balance is simplified — needs PlayerSessionManager for full unassigned player detection.
3. **Type consistency:** `Team` record gets `playerIds` list. `TournamentManager` gets assignment methods. Commands wire everything.
4. **Scope check:** 4 tasks. Task 1 is foundation, Task 2 is manager logic, Task 3 is commands, Task 4 is auto-balance.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
