# EmaraLeague — Requirements

## Functional Requirements

### Core Tournament Engine
1. **Tournament creation** — admins can create tournaments via commands or GUI.
2. **Bracket systems** — single elimination, double elimination, round-robin, swiss.
3. **Matchmaking** — auto-match players/teams; support seeded and random draws.
4. **Registration** — solo and team registration with minimum/maximum player limits.
5. **Arena management** — bind matches to arenas/worlds; auto-load and reset via SlimeWorld templates.
6. **State machine** — `LOBBY → STARTING → INGAME → ENDING → RESETTING` per match.
7. **Spectator mode** — automatic spectator assignment with streaming/casting tools.
8. **Score tracking** — live scoreboards, boss bars, tab lists via Adventure.
9. **Rewards** — integration with Vault, PlayerPoints, and custom item rewards.
10. **Statistics** — per-player and per-team win/loss tracking persisted to SQLite/MySQL.
11. **PlaceholderAPI expansion** — expose `%emaraleague_wins%`, `%emaraleague_rank%`, etc.
12. **Multi-language** — fully configurable messages; defaults in English, Spanish, Portuguese, Russian, Chinese.

### Game Mode Support
13. **Built-in modes** — Duels, Spleef, Sumo, TNT Run as reference modules.
14. **External mode API** — third-party minigames can hook into EmaraLeague via public API.
15. **Generic objective system** — allow tournaments to be won by kills, time, score, or survival.

### Admin & UX
16. **GUI setup editor** — create arenas, spawns, teams, and kits via IF-based inventory GUI.
17. **Commands with Brigadier** — modern tab-complete command system.
18. **Permissions** — full LuckPerms-compatible permission nodes.
19. **Configuration** — Configurate-based YAML config with validation and hot-reload where safe.
20. **Logging & debugging** — SLF4J logging with configurable debug mode.

## Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Minecraft version | Paper 1.21.x+ |
| Java version | Java 21 |
| Build system | Gradle Kotlin DSL + paperweight-userdev + shadow |
| Plugin metadata | `paper-plugin.yml` |
| Database | SQLite default; MySQL/MariaDB optional via HikariCP |
| Caching | Caffeine for in-memory player/session data |
| Scheduler | Folia-ready abstraction (Global/Region/Entity/Async schedulers) |
| World instances | AdvancedSlimePaper (ASWM) for arena templates |
| Proxy support | Velocity (future), BungeeCord legacy compatibility not required |
| Performance | No main-thread blocking for I/O, DB, or Redis operations |
| Security | Prepared statements, input validation, no hardcoded secrets |

## Flows

### Flow 1: Create a Tournament
1. Admin runs `/emaraleague create <name> <mode>`.
2. System prompts for bracket type, max players, team size, arena.
3. Tournament enters `LOBBY` state and opens registration.
4. Players/teams register via `/emaraleague join <name>`.
5. Admin starts tournament; system generates bracket and announces matches.

### Flow 2: Run a Match
1. System moves match to `STARTING`, teleports players, shows countdown.
2. Match enters `INGAME`; external minigame or built-in mode reports results.
3. On result, system updates bracket and assigns winner/loser.
4. Match enters `RESETTING`; world resets via ASWM template reload.
5. Next match scheduled automatically.

### Flow 3: Reward Distribution
1. Tournament ends; system identifies top 1/2/3 placements.
2. Rewards distributed via Vault/PlayerPoints/items.
3. Stats updated in database.
4. Broadcast final results and update leaderboards.

## Acceptance Criteria

- [ ] Plugin loads cleanly on Paper 1.21.x with no errors.
- [ ] Admin can create, start, and complete a single-elimination tournament using built-in Spleef mode.
- [ ] Player stats persist across restarts (SQLite default).
- [ ] Folia scheduler abstraction compiles against both Paper and Folia APIs.
- [ ] PlaceholderAPI placeholders return correct values for online players.
- [ ] Multi-language messages can be switched via config without restart.
- [ ] Arena reset completes in under 5 seconds for a 100x100 world.
- [ ] All DB operations run off the main thread.

## Scope Boundaries

### In Scope (v1.0)
- Tournament engine core.
- Single, double elimination, and round-robin brackets.
- Built-in modes: Duels, Spleef, Sumo, TNT Run.
- SQLite + MySQL persistence.
- PlaceholderAPI, Vault, LuckPerms integration.
- Folia-ready scheduler abstraction.
- GUI editor for basic arena setup.
- Multi-language message config.

### Out of Scope (v1.0; future add-ons)
- Web dashboard.
- Ranked ELO seasons.
- Redis cross-server support.
- Replay system.
- Discord bot integration.
- Velocity proxy integration.
- Anti-cheat direct hooks.

## Monetization Requirements
- Core plugin sold as premium license.
- Add-on architecture in place for future paid modules (Ranked, WebDash, Discord).
- Direct website ready for vouchers/discounts and crypto payments.
- Multi-platform listings: BuiltByBit, SpigotMC, Polymart.
