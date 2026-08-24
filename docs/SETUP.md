# EmaraLeague — Setup Guide

> **Version:** 1.0.1
> **Last Updated:** August 2026

---

## 📋 Requirements

| Requirement | Version | Notes |
|-------------|---------|-------|
| **Server** | Paper 1.21+ | Paper only (Spigot/Bukkit not supported) |
| **Java** | 21+ | Required for Paper 1.21+ |
| **RAM** | 2GB+ recommended | Depends on concurrent matches |
| **Database** | SQLite (built-in) | MySQL optional for large servers |

---

## 🚀 Installation

### Step 1: Download
1. Purchase EmaraLeague from [BuiltByBit](https://builtbybit.com) or [SpigotMC](https://spigotmc.org)
2. Download `EmaraLeague-1.0.1-obfuscated.jar`

### Step 2: Install
1. Stop your server
2. Copy `EmaraLeague-1.0.1-obfuscated.jar` to `plugins/` folder
3. Start server
4. Check console for `EmaraLeague enabled`

### Step 3: Verify
```
[Server] [INFO] [EmaraLeague] EmaraLeague enabled
```

---

## 🎮 First-Time Setup

### Step 1: Create Arena

```
# Create arena
/el arena create MyArena

# Set arena center (stand where you want players to fight)
/el arena setcenter MyArena

# Set lobby spawn (stand where players wait)
/el arena setlobby MyArena

# Verify
/el arena list
```

### Step 2: Create Tournament

```
# Create tournament (modes: duels, spleef, sumo)
/el create SummerCup duels

# Check tournament info
/el info SummerCup
```

### Step 3: Players Join

```
# Players join tournament (auto-team assignment)
/el join SummerCup

# Check teams
/el team list SummerCup
```

### Step 4: Start Tournament

```
# Start tournament (requires 2+ teams with players)
/el start SummerCup

# Players will be teleported to arena
# Countdown begins (10 seconds)
# Match starts automatically
```

---

## ⚙️ Basic Configuration

### config.yml

```yaml
# Countdown duration before match starts (seconds)
countdown-seconds: 10

# Arena settings
arena:
  # Y-level below which players are eliminated (Spleef)
  fall-threshold: 0
  # Auto-assign available arena to match
  auto-assign: true

# Match settings
match:
  # Default game mode for new tournaments
  default-mode: duels
  # Maximum concurrent matches per server
  max-concurrent: 4

# UI settings
ui:
  # Show BossBar countdown
  bossbar-countdown: true
  # Show scoreboard during match
  scoreboard: true
```

### messages.yml

```yaml
# Customize all plugin messages
prefix: "<gold>EmaraLeague <red>»</red> "

tournament-created: "<green>Tournament '<name>' created successfully!"
tournament-joined: "<green>You joined tournament '<name>'!"
tournament-started: "<green>Tournament '<name>' has started!"
# ... more messages
```

---

## 🔧 Common Tasks

### Add More Arenas

```
/el arena create Arena2
/el arena setcenter Arena2
/el arena setlobby Arena2
```

### Create Different Game Mode Tournament

```
/el create SpleefTournament spleef
/el create SumoTournament sumo
```

### Check Match History

```
# View past matches
/el history
```

### Reload Configuration

```
/el reload
```

---

## 🆘 Getting Help

- **Documentation:** [docs.emaralabs.com](https://docs.emaralabs.com)
- **Discord Support:** [discord.gg/emaralabs](https://discord.gg/emaralabs)
- **Bug Reports:** [GitHub Issues](https://github.com/EmaraLabs/emaraleague/issues)

---

## 📚 Next Steps

- Read [COMMANDS.md](COMMANDS.md) for full command reference
- Read [PERMISSIONS.md](PERMISSIONS.md) for permission setup
- Read [CONFIGURATION.md](CONFIGURATION.md) for advanced settings
- Read [FAQ.md](FAQ.md) for troubleshooting
