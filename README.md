# EmaraLeague

**The ultimate tournament & events engine for Minecraft servers.**

Run competitive events, manage brackets, and reward players — all without writing a single line of code.

---

## What is EmaraLeague?

EmaraLeague is a premium Paper plugin that transforms any Minecraft server into a professional esports platform. Create tournaments, manage registrations, automate brackets, spectate matches, and distribute rewards — everything is handled through an intuitive in-game GUI or simple commands.

Whether you're running a small community server or a large network, EmaraLeague scales with your needs.

---

## Supported Versions

| Minecraft Version | Status |
|---|---|
| **1.21.x** | ✅ Fully Supported |
| **1.20.x** | ⚠️ Untested (may work) |
| **1.19.x and below** | ❌ Not Supported |

**Server Software:** Paper 1.21.x+ (Spigot-compatible, Folia-ready)

---

## Key Features

### Tournament Engine
- **4 Bracket Types:** Single Elimination, Double Elimination, Round Robin, Swiss
- **Auto Matchmaking:** Queue-based or seeded draws
- **Team Support:** Solo or team registration (1v1 to 32v32)
- **Live Scoreboards:** Real-time updates via Adventure BossBar & TabList

### Game Modes (Built-in)
- **Duels** — Classic PvP with kit selection
- **Spleef** — Block-breaking elimination
- **Sumo** — Knockback arena battles
- **TNT Run** — Floor decay survival

### Arena Management
- **SlimeWorld Templates:** Instant arena load/reset via AdvancedSlimePaper
- **Multi-Arena:** Run multiple matches simultaneously
- **Auto-Reset:** Worlds restore in under 5 seconds

### Player Experience
- **Spectator Mode:** Auto-assign with streaming/casting tools
- **Statistics:** Track wins, losses, ELO (addon), and leaderboards
- **Rewards:** Vault economy, PlayerPoints, custom items

### Admin Tools
- **GUI Editor:** Create arenas, teams, and tournaments in-game
- **Multi-language:** English, Spanish, Portuguese, Russian, Chinese
- **PlaceholderAPI:** `%emaraleague_wins%`, `%emaraleague_rank%`, etc.

---

## Compatibility

| Plugin | Status | Purpose |
|---|---|---|
| **Vault** | ✅ Soft Dependency | Economy rewards |
| **PlaceholderAPI** | ✅ Soft Dependency | Placeholder expansion |
| **LuckPerms** | ✅ Soft Dependency | Permission contexts |
| **PlayerPoints** | ✅ Soft Dependency | Alternative economy |
| **AdvancedSlimePaper** | ⚠️ Optional | Slime world templates (recommended) |

**Proxy Support:** Velocity (future v2.0), BungeeCord (legacy, not recommended)

---

## Dependencies

### Required (Server-Side)
- **Paper 1.21.x+** (or Folia)

### Optional (Plugin Dependencies)
- Vault 1.7.1+
- PlaceholderAPI 2.11.6+
- LuckPerms 5.4+
- PlayerPoints (latest)

### Future (v2.0+)
- Redis 5.0+ (multi-server sync)
- Lettuce 6.4+ (Redis client)

---

## Installation

1. Download `EmaraLeague-x.x.x.jar` from your purchase source
2. Place in `plugins/` folder
3. Restart server
4. Run `/emaraleague setup` to begin configuration
5. Optional: Install AdvancedSlimePaper for better arena performance

**Full setup guide:** [docs.emaralabs.com](https://docs.emaralabs.com)

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/emaraleague create <name> <mode>` | `emaraleague.admin` | Create tournament |
| `/emaraleague join <name>` | `emaraleague.play` | Join tournament |
| `/emaraleague leave` | `emaraleague.play` | Leave tournament |
| `/emaraleague start <name>` | `emaraleague.admin` | Start tournament |
| `/emaraleague info <name>` | `emaraleague.play` | View tournament info |
| `/emaraleague spectate <match>` | `emaraleague.spectate` | Spectate match |

**Full command list:** `/emaraleague help`

---

## Configuration

All configuration is done via `plugins/EmaraLeague/config.yml` or in-game GUI.

**Key sections:**
- `database` — SQLite (default) or MySQL
- `messages` — Multi-language support
- `brackets` — Bracket type settings
- `rewards` — Vault/PlayerPoints/item rewards
- `arenas` — SlimeWorld template paths

---

## API for Developers

EmaraLeague exposes a public API for third-party minigame integration.

```java
EmaraLeagueAPI api = EmaraLeague.getAPI();
api.registerGameMode(new MyCustomGameMode());
```

**Documentation:** [docs.emaralabs.com/api](https://docs.emaralabs.com/api)

---

## Support

- **Discord:** [discord.gg/emaralabs](https://discord.gg/emaralabs)
- **Docs:** [docs.emaralabs.com](https://docs.emaralabs.com)
- **Email:** support@emaralabs.com

---

## License

**PROPRIETARY — All Rights Reserved**

Copyright (c) 2026 Haris / EmaraLabs.

This software is developed solely by Haris (EmaraLabs). No contributors.
Purchase grants a license to **use** the compiled plugin only.
Source code is never distributed.

See [LICENSE.md](LICENSE.md) for full terms.

---

## Changelog

### v1.0.0 (In Development)
- Initial release
- 4 bracket types
- 4 built-in game modes
- GUI editor
- Multi-language support
- PlaceholderAPI expansion

**Roadmap:** [github.com/EmaraLabs/emaraleague/projects](https://github.com/EmaraLabs/emaraleague/projects)

---

Made with ❤️ by **Haris** | [emaralabs.com](https://emaralabs.com)
