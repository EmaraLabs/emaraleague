# EmaraLeague — System Notes
> **For:** Haris (Developer & Owner)
> **Last Updated:** 2026-08-24
> **Version:** v1.0.1 (current) → v1.1 (next)

---

## 🎯 Apa Itu EmaraLeague?

Plugin Minecraft premium untuk **tournament & events**. Server owner boleh buat competitive tournaments (PvP, Spleef, Sumo, dll) dengan automated brackets, matchmaking, spectator tools, dan monetized add-ons.

**Target:** Jual di BuiltByBit, SpigotMC, Polymart.

---

## 🏗️ Architecture — Macam Mana Sistem Ni Berfungsi

```
┌─────────────────────────────────────────────────────────┐
│                    BOOTSTRAP (bootstrap/)                │
│  Entry point: EmaraLeaguePlugin.java                    │
│  - Register commands, listeners, managers               │
│  - Wire semua dependencies                              │
│  - Build final JAR (Shadow + ProGuard)                  │
└─────────────────────────────────────────────────────────┘
                           │
    ┌──────────────────────┼──────────────────────┐
    ▼                      ▼                      ▼
┌─────────┐         ┌──────────┐          ┌──────────┐
│  CORE   │         │  API     │          │ EDITOR   │
│ (core/) │         │ (api/)   │          │(editor/) │
│         │         │          │          │          │
│ TournamentManager  │ EmaraLeagueAPI     │ GuiEditor │
│ ArenaManager       │ EmaraAddon         │ (v1.1)   │
│ MatchEngine        │ (for addons)       │          │
│ GameModeRegistry   │                    │          │
│ SpectatorManager   │                    │          │
│ PlayerStats        │                    │          │
│ ConfigManager      │                    │          │
└─────────┘         └──────────┘          └──────────┘
    │
    ▼
┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ MODULES │  │INTEGRA- │  │INFRA-    │  │ ADDONS   │
│(modules/)│  │TIONS    │  │STRUCTURE │  │(addons/) │
│         │  │(integrations/)│(infrastructure/)│         │
│ duels   │  │ vault    │  │ database │  │ ranked   │
│ spleef  │  │ placeholderapi│ cache   │  │ discord  │
│ sumo    │  │ luckperms│  │ config   │  │ replay   │
│ tnt-run │  │ playerpoints│ logging  │  │ web-dash │
│ parkour │  │          │  │ security │  │ spectator│
│ capture │  │          │  │          │  │          │
└─────────┘  └──────────┘  └──────────┘  └──────────┘
```

### Module Breakdown

| Module | Function | Status |
|--------|----------|--------|
| **bootstrap** | Plugin entry, command wiring, JAR build | ✅ Stable |
| **core** | Tournament logic, match engine, arena, player session | ✅ Stable |
| **api** | Public API for addons (EmaraLeagueAPI, EmaraAddon) | ✅ Stable |
| **editor** | In-game GUI editor (Triumph GUI) | 🟡 v1.1 (skeleton) |
| **modules/duels** | Duels game mode (1v1 PvP) | ✅ Stable |
| **modules/spleef** | Spleef game mode (break blocks, fall = lose) | ✅ Stable |
| **modules/sumo** | Sumo game mode (push out of ring) | ✅ Stable |
| **modules/tnt-run** | TNTRun (blocks disappear) | 🟡 v1.1 (skeleton) |
| **modules/parkour** | Parkour race | 🟡 v1.1 (skeleton) |
| **modules/capture-the-flag** | CTF | 🟡 v1.1 (skeleton) |
| **integrations/vault** | Economy support | ✅ Stable |
| **integrations/placeholderapi** | PlaceholderAPI placeholders | ✅ Stable |
| **integrations/luckperms** | LuckPerms permission contexts | ✅ Stable |
| **integrations/playerpoints** | PlayerPoints economy | ✅ Stable |
| **infrastructure/database** | SQLite/MySQL via Exposed + HikariCP | ✅ Stable |
| **infrastructure/cache** | Caffeine cache | ✅ Stable |
| **infrastructure/config** | Configurate YAML | ✅ Stable |
| **infrastructure/logging** | SLF4J logging | ✅ Stable |
| **infrastructure/security** | Input validation, sanitization | ✅ Stable |
| **addons/ranked** | ELO ranked system (paid addon) | ❌ v1.1+ |
| **addons/discord** | Discord bot integration | ❌ v1.2 |
| **addons/replay** | Match replay system | ❌ v1.3 |
| **addons/web-dashboard** | Web UI for management | ❌ v1.2 |
| **addons/spectator-tools** | Advanced spectator features | ❌ v1.1+ |

---

## ✅ Features Yang Dah Ada (v1.0.1)

### Core Tournament
| Feature | Command | Status |
|---------|---------|--------|
| Create tournament | `/el create <name> <mode>` | ✅ |
| Join tournament | `/el join <tournament>` | ✅ |
| Leave tournament | `/el leave` | ✅ |
| Delete tournament | `/el delete <tournament>` | ✅ |
| Cancel tournament | `/el cancel <tournament>` | ✅ |
| Start tournament | `/el start <tournament>` | ✅ |
| View info | `/el info <tournament>` | ✅ |
| View history | `/el history` | ✅ |
| View stats | `/el stats` | ✅ |

### Team Management
| Feature | Command | Status |
|---------|---------|--------|
| Join team | `/el team join <tournament> <team>` | ✅ |
| Leave team | `/el team leave` | ✅ |
| List teams | `/el team list <tournament>` | ✅ |
| Auto-assign team | (auto on join) | ✅ |

### Arena Management
| Feature | Command | Status |
|---------|---------|--------|
| Create arena | `/el arena create <name>` | ✅ |
| Set center | `/el arena setcenter <name>` | ✅ |
| Set lobby | `/el arena setlobby <name>` | ✅ |
| Set spawn | `/el arena setspawn <name> <a\|b>` | ✅ |
| List arenas | `/el arena list` | ✅ |
| Delete arena | `/el arena delete <name>` | ✅ |
| Auto-assign arena | (auto on match start) | ✅ |
| Arena reset | (auto after match) | ✅ |

### Match Features
| Feature | Status |
|---------|--------|
| Single Elimination bracket | ✅ |
| Match countdown (bossbar) | ✅ |
| Match timeout | ✅ |
| Win condition evaluator | ✅ |
| Inventory save/restore | ✅ |
| Disconnect grace period | ✅ |
| Max concurrent matches | ✅ (config: `match.max-concurrent`) |

### Player Features
| Feature | Command | Status |
|---------|---------|--------|
| Spectate match | `/el spectate <tournament>` | ✅ NEW in v1.0.1 |
| Rejoin match | `/el rejoin` | ✅ NEW in v1.0.1 |
| Player statistics | (tracked automatically) | ✅ |

### Game Modes (v1.0.1)
| Mode | Status | Notes |
|------|--------|-------|
| Duels | ✅ Full | 1v1 PvP, kill to win |
| Spleef | ✅ Full | Break blocks, fall = lose |
| Sumo | ✅ Full | Push out of ring |
| TNTRun | 🟡 Skeleton | Blocks disappear (not wired) |
| Parkour | 🟡 Skeleton | Race to finish (not wired) |
| CTF | 🟡 Skeleton | Capture flag (not wired) |

### Technical / Professional
| Feature | Status |
|---------|--------|
| ProGuard obfuscation | ✅ |
| SLF4J logging (no warnings) | ✅ |
| Config validation | ✅ |
| Input sanitization | ✅ |
| Message templates (MiniMessage) | ✅ |
| Tab completion | ✅ |
| Permission system | ✅ |
| Error feedback | ✅ |
| Consistent status formatting | ✅ |
| Centralized color/theme | ✅ |

---

## 🟡 Features Dalam Progress (v1.1)

### Week 1: GUI Foundation
| Task | Deliverable | Status |
|------|-------------|--------|
| Setup Triumph GUI | `EmaraGui` wrapper class | ⬜ Not started |
| Custom theme layer | `EmaraTheme` colors, borders, sounds | ⬜ Not started |
| Arena list GUI | `/el gui arenas` | ⬜ Not started |
| Arena create GUI | Inventory-based arena setup | ⬜ Not started |

### Week 2: GUI Editor
| Task | Deliverable | Status |
|------|-------------|--------|
| Tournament create GUI | `/el gui tournaments` | ⬜ Not started |
| Team setup GUI | Drag-drop team assignment | ⬜ Not started |
| Multi-language system | `messages_en.yml`, `messages_es.yml`, etc. | ⬜ Not started |

### Week 3: Game Modes
| Task | Deliverable | Status |
|------|-------------|--------|
| TNTRun full implementation | Playable mode | ⬜ Not started |
| Parkour full implementation | Playable mode | ⬜ Not started |
| CTF full implementation | Playable mode | ⬜ Not started |

### Week 4: Polish
| Task | Deliverable | Status |
|------|-------------|--------|
| Testing & bug fixes | Stable v1.1.0 | ⬜ Not started |
| Documentation update | GUI docs, multi-language docs | ⬜ Not started |
| Release | v1.1.0 | ⬜ Not started |

---

## ❌ Features Future (Post-v1.1)

### v1.2 — Revenue Addons
| Addon | Price | Features |
|-------|-------|----------|
| Ranked System | $9.99 | ELO, seasons, divisions, leaderboards |
| Discord Bot | $7.99 | Auto-announce results, match history |
| Web Dashboard | $14.99 | Browser-based tournament management |

### v1.3 — Advanced
| Addon | Price | Features |
|-------|-------|----------|
| Replay System | $12.99 | Record & playback matches |
| Multi-server | TBD | Cross-server tournaments |

---

## 🔧 Configuration Reference

### config.yml
```yaml
# Language (v1.1)
language: en

# Debug mode
debug: false

# Match settings
countdown-seconds: 10
match:
  default-mode: duels
  max-concurrent: 4    # NEW in v1.0.1

# Arena settings
arena:
  fall-threshold: 0    # Y-level for elimination
  auto-assign: true

# UI settings
ui:
  bossbar-countdown: true
  scoreboard: true

# Database (SQLite default, MySQL optional)
database:
  type: sqlite
  # mysql:
  #   host: localhost
  #   port: 3306
  #   database: emaraleague
  #   username: root
  #   password: password
```

### messages.yml
All messages customizable via MiniMessage format. Placeholders: `<name>`, `<mode>`, `<status>`, `<permission>`, `<usage>`.

---

## 📦 Build & Release

### Build Commands
```bash
# Normal build
./gradlew build

# Shadow JAR (all dependencies bundled)
./gradlew :bootstrap:shadowJar

# Obfuscated production JAR
./gradlew :bootstrap:obfuscate
```

### JAR Files
| File | Size | Use For |
|------|------|---------|
| `EmaraLeague-1.0.1.jar` | ~28.8 MB | Development/testing |
| `EmaraLeague-1.0.1-obfuscated.jar` | ~15.8 MB | **Production release** |

### Version History
| Version | Date | Changes |
|---------|------|---------|
| v1.0.0 | 2026-08-22 | Initial release |
| v1.0.1 | 2026-08-24 | ProGuard, spectate/rejoin, max-concurrent |

---

## 🎮 Game Mode Details

### Duels (1v1 PvP)
- **Win:** Kill opponent
- **Arena:** Small enclosed arena
- **Spectate:** Yes
- **Rejoin:** Yes

### Spleef
- **Win:** Last player standing (don't fall)
- **Arena:** Platform with breakable blocks
- **Spectate:** Yes
- **Rejoin:** Yes

### Sumo
- **Win:** Push opponent out of ring
- **Arena:** Small circular platform
- **Spectate:** Yes
- **Rejoin:** Yes

### TNTRun (v1.1)
- **Win:** Last player standing (blocks disappear)
- **Arena:** Multi-layer platform
- **Spectate:** Yes
- **Rejoin:** Yes

### Parkour (v1.1)
- **Win:** First to finish course
- **Arena:** Parkour course with checkpoints
- **Spectate:** Yes
- **Rejoin:** Yes

### Capture The Flag (v1.1)
- **Win:** Capture enemy flag X times
- **Arena:** Two bases with flags
- **Spectate:** Yes
- **Rejoin:** Yes

---

## 🔌 API for Addons (v1.1+)

```java
// Get plugin instance
EmaraLeagueAPI api = EmaraLeaguePlugin.getInstance().getAPI();

// Register addon
api.registerAddon(new MyAddon());

// Register game mode
api.registerGameMode(new MyGameMode());

// Broadcast to tournament
api.broadcastToTournament("SummerCup", Component.text("Hello!"));
```

---

## 📝 Notes for Decision Making

### When to Add Feature?
| Scenario | Action |
|----------|--------|
| Buyer request (paid) | Add to v1.1 or addon |
| Buyer request (free) | Consider for v1.2 |
| Marketplace trend | Evaluate competitor |
| Technical debt | Fix immediately |
| Security issue | Hotfix v1.0.x |

### When to Postpone?
| Scenario | Reason |
|----------|--------|
| Complex feature (web dashboard) | Save for v1.2 |
| Low demand | Wait for more requests |
| Requires refactor | Plan for major version |
| Third-party dependency | Wait for stable API |

### Priority Matrix
|  | High Impact | Low Impact |
|--|-------------|------------|
| **Easy** | Do now | Do later |
| **Hard** | Plan carefully | Postpone |

---

## 🤔 Common Questions

**Q: Kenapa ProGuard penting?**
A: Buyer boleh decompile JAR dan curi code. Obfuscation buat code susah dibaca.

**Q: Kenapa tak semua game modes aktif?**
A: v1.0 focus on 3 core modes. v1.1 akan complete TNTRun, Parkour, CTF.

**Q: Bila nak buat GUI?**
A: v1.1 Week 1-2. Triumph GUI + Adventure Components + custom layer.

**Q: Macam mana nak tambah game mode baru?**
A: 1. Create class implements `GameMode`, 2. Register in `GameModeRegistry`, 3. Add to `GAME_MODES` list in command.

**Q: Macam mana nak tambah language?**
A: 1. Create `messages_<lang>.yml`, 2. Add to config `language: <lang>`, 3. Plugin auto-load.

---

## 📞 Support & Maintenance

| Channel | Response Time |
|---------|---------------|
| Discord | < 24 hours |
| BuiltByBit PM | < 48 hours |
| Email | < 72 hours |

**Update Policy:**
- Bug fixes: v1.0.x (immediate)
- Minor features: v1.1 (monthly)
- Major features: v2.0 (quarterly)

---

*End of notes. Keep this file updated as the project evolves.*
