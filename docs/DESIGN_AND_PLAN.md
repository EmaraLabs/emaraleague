# EmaraLeague — Design & Implementation Plan

**Version:** 1.0  
**Date:** 2026-08-21  
**Goal:** Build a scalable, monorepo-structured Minecraft tournament engine plugin with clean separation of core engine, game modules, integrations, and future add-ons.

---

## 1. Monorepo Strategy

### Why Monorepo?

| Benefit | Explanation |
|---|---|
| **Single source of truth** | Core engine, built-in modes, integrations, and addons live together. |
| **Atomic releases** | Update core + module in one commit, version bump coordinated. |
| **Shared infrastructure** | Build scripts, CI, testing, and quality gates centralized. |
| **Simplified dependency management** | Gradle composite builds; no version mismatch between modules. |
| **Easier refactoring** | Rename a core class → all modules compile or fail fast. |

### Monorepo Layout

```
EmaraLeague/
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Composite build + module includes
├── gradle.properties             # Versions, coordinates, publishing config
├── gradle/wrapper/               # Gradle wrapper
├── .github/workflows/            # CI/CD (build, test, publish)
├── docs/                         # Project docs (CONTEXT, REQUIREMENTS, HERMES_DELIVERY, DESIGN)
├── .hermes/plans/                # HDS implementation plans
├── scripts/                      # Build/dev utility scripts
│
├── core/                         # Neutral engine — no game-specific logic
│   ├── build.gradle.kts
│   └── src/main/java/com/emaralabs/emaraleague/core/
│       ├── arena/                # Arena, ArenaManager, state machine
│       ├── game/                 # GameMode API interface
│       ├── team/                 # Team, queue, balancer
│       ├── player/               # PlayerSession, spectator
│       ├── reward/               # Reward/economy hooks
│       ├── stats/                # Stats tracking
│       ├── world/                # SlimeWorld adapter (ASWM)
│       ├── ui/                   # Adventure components, BossBar, TabList, GUI
│       ├── data/                 # PDC + Configurate + DB + Caffeine
│       ├── scheduler/            # Folia-ready scheduler abstraction
│       └── bracket/              # Bracket generators (single, double, RR, swiss)
│
├── modules/                      # Built-in game modes (content packs)
│   ├── duels/
│   ├── spleef/
│   ├── sumo/
│   └── tnt-run/
│
├── integrations/                 # Third-party plugin hooks
│   ├── vault/
│   ├── placeholderapi/
│   ├── luckperms/
│   └── playerpoints/
│
├── addons/                       # Premium addons (v2.0+)
│   ├── ranked/                   # ELO seasons, divisions
│   ├── web-dashboard/            # Web UI for tournament management
│   ├── discord/                  # Discord bot integration
│   └── spectator-tools/          # Advanced casting/replay tools
│
├── api/                          # Public API for third-party developers
│   ├── build.gradle.kts
│   └── src/main/java/com/emaralabs/emaraleague/api/
│       ├── tournament/
│       ├── bracket/
│       ├── game/
│       └── player/
│
├── infrastructure/               # Cross-cutting concerns
│   ├── database/                 # Exposed/JOOQ + HikariCP
│   ├── cache/                    # Caffeine, Redis (future)
│   ├── config/                   # Configurate loaders
│   ├── logging/                  # SLF4J setup
│   └── security/                 # Validation, rate limiting, permission checks
│
├── editor/                       # GUI setup editor (IF-based)
│   └── src/main/java/com/emaralabs/emaraleague/editor/
│
└── bootstrap/                    # Plugin bootstrap + paper-plugin.yml
    └── src/main/java/com/emaralabs/emaraleague/
        ├── EmaraLeaguePlugin.java
        ├── command/
        ├── listener/
        └── config/
```

---

## 2. Architecture Design

### Core Principles

1. **Core-neutral engine** — `core/` knows nothing about specific game modes.
2. **Interface-driven modules** — Every game mode implements `GameMode` interface.
3. **Folia-ready scheduler** — Abstract scheduler interface; Paper and Folia implementations.
4. **Async-first data** — All DB/Redis/network operations off main thread.
5. **Immutable state where possible** — Bracket state, tournament state use immutable objects.

### Key Interfaces

```java
// GameMode API (core/game/GameMode.java)
public interface GameMode {
    String getId();
    String getDisplayName();
    GameModeSettings getDefaultSettings();
    void onMatchStart(MatchContext context);
    void onMatchTick(MatchContext context);
    void onMatchEnd(MatchContext context, MatchResult result);
    WinCondition getWinCondition();
}

// Scheduler Abstraction (core/scheduler/EmaraScheduler.java)
public interface EmaraScheduler {
    ScheduledTask runAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);
    ScheduledTask runAsync(Runnable task);
    ScheduledTask runOnRegion(Runnable task, Location location);
    ScheduledTask runOnEntity(Runnable task, Entity entity);
}

// Bracket Generator (core/bracket/BracketGenerator.java)
public interface BracketGenerator {
    Bracket generate(List<Team> participants, BracketSettings settings);
    void advance(Bracket bracket, MatchResult result);
    Optional<Match> nextMatch(Bracket bracket);
}
```

### Data Model (Simplified)

```java
public record Tournament(
    UUID id,
    String name,
    GameMode mode,
    BracketType bracketType,
    TournamentState state,
    List<Team> teams,
    Bracket bracket,
    Arena arena,
    Instant createdAt
) {}

public record Match(
    UUID id,
    Team teamA,
    Team teamB,
    MatchState state,
    MatchResult result,
    Instant scheduledAt
) {}

public record Team(
    UUID id,
    String name,
    List<Player> players,
    int seed
) {}

public record Arena(
    UUID id,
    String name,
    World templateWorld,
    Location center,
    int radius,
    ArenaState state
) {}
```

---

## 3. Scalability Plan

### Phase 1: Single Server (v1.0)
- SQLite local database.
- All tournaments run on one server.
- In-memory cache via Caffeine.

### Phase 2: Multi-Server (v2.0)
- MySQL/MariaDB central database.
- Redis for cross-server tournament state and cache sync.
- Velocity proxy integration for queue balancing.

### Phase 3: Network-Wide Events (v3.0)
- Dedicated event server instances spun up via Docker/Kubernetes.
- Tournament state replicated via Redis pub/sub.
- Web dashboard for network-wide management.

### Scaling Tactics

| Concern | Strategy |
|---|---|
| **Player load** | Async queue processing; limit concurrent matches per server. |
| **World instances** | ASWM slime world templates; load-on-demand; auto-cleanup. |
| **Database** | HikariCP connection pooling; read replicas for leaderboards. |
| **Cache** | Caffeine L1 + Redis L2; invalidation via pub/sub. |
| **Scheduler** | Folia-ready; entity/region schedulers for per-arena isolation. |

---

## 4. Implementation Plan (Bite-Sized Tasks)

### Task Group A: Project Bootstrap

**Task A1: Initialize Gradle Monorepo**
- Create `settings.gradle.kts` with composite build includes for all modules.
- Configure `build.gradle.kts` root with Java 21, Paper 1.21.x, and dependency constraints.
- Add `gradle.properties` with version coordinates.

**Task A2: Setup CI Pipeline**
- Create `.github/workflows/build.yml` for PR checks (compile + test).
- Create `.github/workflows/publish.yml` for release builds to Hangar/Modrinth.

**Task A3: Create Module Skeletons**
- Generate `core/`, `api/`, `modules/`, `integrations/`, `infrastructure/`, `bootstrap/` directories with empty `build.gradle.kts`.

### Task Group B: Core Engine Foundation

**Task B1: Scheduler Abstraction**
- Create `EmaraScheduler` interface.
- Implement `PaperScheduler` (BukkitScheduler wrapper).
- Implement `FoliaScheduler` (Global/Region/Entity/Async schedulers).
- Unit test with MockBukkit.

**Task B2: Arena & World Management**
- Create `Arena`, `ArenaManager`, `ArenaState`.
- Integrate ASWM for template loading and reset.
- Write integration test for arena reset.

**Task B3: Tournament & Match State Machine**
- Create `Tournament`, `Match`, `Team` records.
- Implement `TournamentState` and `MatchState` enums.
- Create `TournamentManager` with state transition validation.

**Task B4: Bracket Generators**
- Implement `SingleEliminationBracket`.
- Implement `DoubleEliminationBracket`.
- Implement `RoundRobinBracket`.
- Write unit tests for each bracket type.

**Task B5: Player & Team Management**
- Create `PlayerSession`, `Team`, `QueueManager`.
- Implement spectator mode with Adventure BossBar/TabList.

### Task Group C: Data Layer

**Task C1: Database Setup**
- Configure HikariCP with SQLite (dev) and MySQL (prod).
- Create Exposed/JOOQ schema for tournaments, matches, teams, players, stats.

**Task C2: Caching Layer**
- Integrate Caffeine for tournament/player session cache.
- Write cache invalidation strategies.

**Task C3: Repository Pattern**
- Create `TournamentRepository`, `MatchRepository`, `StatsRepository`.
- Implement async read/write methods.

### Task Group D: Game Modules

**Task D1: Duels Module**
- Implement `DuelsGameMode` with kit selection and win conditions.

**Task D2: Spleef Module**
- Implement `SpleefGameMode` with block break tracking.

**Task D3: Sumo Module**
- Implement `SumoGameMode` with knockback tracking.

**Task D4: TNT Run Module**
- Implement `TNTRunGameMode` with block decay timer.

### Task Group E: Integrations

**Task E1: Vault Integration**
- Hook economy rewards via Vault API.

**Task E2: PlaceholderAPI Expansion**
- Create `EmaraLeagueExpansion` for `%emaraleague_wins%`, `%emaraleague_rank%`, etc.

**Task E3: LuckPerms Integration**
- Context-based permissions for tournament participants/admins.

### Task Group F: Commands & UI

**Task F1: Brigadier Commands**
- Create `/emaraleague create|join|leave|start|info` command tree.

**Task F2: GUI Editor**
- Build IF-based GUI for arena creation, team setup, tournament management.

**Task F3: Multi-language Messages**
- Implement Configurate message bundles for EN/ES/PT/RU/ZH.

### Task Group G: Testing & Quality

**Task G1: Unit Tests**
- Cover bracket generators, state machines, data repositories.

**Task G2: Integration Tests**
- Test tournament flow end-to-end with MockBukkit + paperweight dev server.

**Task G3: Performance Tests**
- Benchmark arena reset time, match tick performance, DB query latency.

### Task Group H: Packaging & Launch

**Task H1: Shadow JAR Build**
- Configure Gradle shadow with relocation for all shaded dependencies.

**Task H2: Marketplace Listings**
- Write BuiltByBit, SpigotMC, Polymart listings with screenshots/video.

**Task H3: Direct Website**
- Setup landing page with pricing, docs, and purchase integration.

---

## 5. Acceptance Criteria

- [ ] Monorepo builds successfully with `gradle build`.
- [ ] Core engine passes all unit tests (brackets, state machine, scheduler).
- [ ] Duels tournament can run end-to-end on a dev server.
- [ ] Arena reset completes in <5s for a 100x100 world.
- [ ] Player stats persist across restarts.
- [ ] PlaceholderAPI returns correct values for online players.
- [ ] Folia scheduler compiles against Folia API.
- [ ] Multi-language messages switchable via config.

---

## 6. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| **Scope creep** | Stick to v1.0 requirements; addons deferred to v2.0. |
| **Folia complexity** | Abstract scheduler early; test on Folia dev builds. |
| **DB performance** | Async all queries; use Caffeine aggressively. |
| **ASWM stability** | Pin to stable release; fallback to normal world copy if needed. |
| **Marketplace approval delays** | Submit listings early; prepare screenshots/video in advance. |

---

## 7. Next Actions

1. **Approve this design + plan.**
2. **Initialize monorepo structure** (Task A1–A3).
3. **Begin Task Group B** (Core Engine Foundation).
