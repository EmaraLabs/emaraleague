# EmaraLeague — Command Reference

> **Version:** 1.0.0
> **Last Updated:** August 2026

---

## 📋 Command Overview

| Command | Description | Permission |
|---------|-------------|------------|
| `/el create` | Create tournament | `emaraleague.create` |
| `/el join` | Join tournament | `emaraleague.play` |
| `/el leave` | Leave tournament | `emaraleague.play` |
| `/el start` | Start tournament | `emaraleague.admin` |
| `/el info` | Tournament info | `emaraleague.use` |
| `/el team` | Team management | `emaraleague.play` |
| `/el arena` | Arena management | `emaraleague.admin` |
| `/el help` | Show help | `emaraleague.use` |
| `/el reload` | Reload config | `emaraleague.reload` |

**Aliases:** `/el`, `/emaraleague`, `/league`

---

## 🏆 Tournament Commands

### `/el create <name> <mode>`

Create a new tournament.

**Arguments:**
- `name` — Tournament name (3-24 chars, alphanumeric)
- `mode` — Game mode (`duels`, `spleef`, `sumo`)

**Example:**
```
/el create SummerCup duels
/el create SpleefTournament spleef
/el create SumoChampionship sumo
```

**Permission:** `emaraleague.create` (default: op)

---

### `/el join <tournament>`

Join a tournament. Auto-assigns you to a team.

**Arguments:**
- `tournament` — Tournament name

**Example:**
```
/el join SummerCup
```

**Permission:** `emaraleague.play` (default: true)

**Notes:**
- Auto-creates teams if none exist
- Auto-assigns to team with fewest players
- Cannot join if already registered

---

### `/el leave`

Leave current tournament. Removes you from team.

**Example:**
```
/el leave
```

**Permission:** `emaraleague.play` (default: true)

---

### `/el start <tournament>`

Start a tournament. Requires 2+ teams with players.

**Arguments:**
- `tournament` — Tournament name

**Example:**
```
/el start SummerCup
```

**Permission:** `emaraleague.admin` (default: op)

**Requirements:**
- Minimum 2 teams
- Each team has 1+ players
- Tournament in REGISTRATION state

---

### `/el info <tournament>`

Show tournament information.

**Arguments:**
- `tournament` — Tournament name

**Example:**
```
/el info SummerCup
```

**Permission:** `emaraleague.use` (default: true)

**Output:**
```
Tournament: SummerCup
Mode: duels
State: REGISTRATION
Teams: 2
Players: 4
```

---

## 👥 Team Commands

### `/el team join <tournament> <team>`

Join a specific team in a tournament.

**Arguments:**
- `tournament` — Tournament name
- `team` — Team name

**Example:**
```
/el team join SummerCup Alpha
```

**Permission:** `emaraleague.play` (default: true)

**Notes:**
- Cannot join if already in another team
- Team must exist in tournament

---

### `/el team leave`

Leave current team.

**Example:**
```
/el team leave
```

**Permission:** `emaraleague.play` (default: true)

---

### `/el team list <tournament>`

List all teams in a tournament.

**Arguments:**
- `tournament` — Tournament name

**Example:**
```
/el team list SummerCup
```

**Permission:** `emaraleague.use` (default: true)

**Output:**
```
Teams in SummerCup:
- Alpha (2 players)
- Beta (1 player)
```

---

## 🏟️ Arena Commands

### `/el arena create <name>`

Create a new arena.

**Arguments:**
- `name` — Arena name (3-32 chars, alphanumeric)

**Example:**
```
/el arena create MyArena
```

**Permission:** `emaraleague.admin` (default: op)

---

### `/el arena setcenter <name>`

Set arena center location. Stand where you want players to fight.

**Arguments:**
- `name` — Arena name

**Example:**
```
/el arena setcenter MyArena
```

**Permission:** `emaraleague.admin` (default: op)

**Notes:**
- Must be standing in the world
- Center is where players teleport for match

---

### `/el arena setlobby <name>`

Set arena lobby spawn. Stand where players wait before match.

**Arguments:**
- `name` — Arena name

**Example:**
```
/el arena setlobby MyArena
```

**Permission:** `emaraleague.admin` (default: op)

---

### `/el arena list`

List all arenas.

**Example:**
```
/el arena list
```

**Permission:** `emaraleague.use` (default: true)

**Output:**
```
Arenas:
- MyArena (LOBBY)
- Arena2 (INGAME)
```

---

### `/el arena delete <name>`

Delete an arena.

**Arguments:**
- `name` — Arena name

**Example:**
```
/el arena delete MyArena
```

**Permission:** `emaraleague.admin` (default: op)

---

## ⚙️ Utility Commands

### `/el help [command]`

Show help for all commands or specific command.

**Arguments:**
- `command` — Optional specific command

**Example:**
```
/el help
/el help create
```

**Permission:** `emaraleague.use` (default: true)

---

### `/el reload`

Reload configuration files.

**Example:**
```
/el reload
```

**Permission:** `emaraleague.reload` (default: op)

**Reloads:**
- `config.yml`
- `messages.yml`

---

## 🔒 Permission Defaults

| Permission | Default | Description |
|------------|---------|-------------|
| `emaraleague.use` | true | Basic commands |
| `emaraleague.play` | true | Join/leave tournaments |
| `emaraleague.create` | op | Create tournaments |
| `emaraleague.admin` | op | Admin commands |
| `emaraleague.reload` | op | Reload config |

---

## 📝 Notes

- All commands support tab completion
- Commands are case-insensitive
- Use `/el help <command>` for detailed help
- Aliases work for all commands (`/el`, `/league`, `/emaraleague`)
