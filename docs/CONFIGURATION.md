# EmaraLeague — Configuration Guide

> **Version:** 1.0.0
> **Last Updated:** August 2026

---

## 📁 Configuration Files

| File | Location | Description |
|------|----------|-------------|
| `config.yml` | `plugins/EmaraLeague/` | Main configuration |
| `messages.yml` | `plugins/EmaraLeague/` | Customizable messages |

---

## ⚙️ config.yml

### Full Configuration

```yaml
# EmaraLeague Configuration
# Documentation: https://docs.emaralabs.com/configuration

# Countdown duration before match starts (seconds)
# Default: 10
# Range: 5-60
countdown-seconds: 10

# Arena settings
arena:
  # Y-level below which players are eliminated (Spleef)
  # Default: 0
  # Set to your arena's kill zone Y-level
  fall-threshold: 0

  # Auto-assign available arena to match
  # Default: true
  # If false, matches must be manually assigned to arenas
  auto-assign: true

# Match settings
match:
  # Default game mode for new tournaments
  # Default: duels
  # Options: duels, spleef, sumo
  default-mode: duels

  # Maximum concurrent matches per server
  # Default: 4
  # Higher values require more arenas and RAM
  max-concurrent: 4

# Database settings
database:
  # SQLite file path (relative to plugin folder)
  # Default: emaraleague.db
  path: emaraleague.db

  # MySQL settings (uncomment for production)
  # host: localhost
  # port: 3306
  # database: emaraleague
  # username: root
  # password: password

# UI settings
ui:
  # Show BossBar countdown during match start
  # Default: true
  bossbar-countdown: true

  # Show scoreboard during match
  # Default: true
  scoreboard: true
```

---

## 🔧 Configuration Options

### countdown-seconds

**Type:** `integer`
**Default:** `10`
**Range:** `5-60`

Duration of countdown before match starts.

**Example:**
```yaml
countdown-seconds: 15  # 15 second countdown
```

---

### arena.fall-threshold

**Type:** `double`
**Default:** `0`

Y-level below which players are eliminated in Spleef mode.

**Example:**
```yaml
arena:
  fall-threshold: -10  # Eliminate below Y=-10
```

**Notes:**
- Set to your arena's kill zone Y-level
- Players below this Y are eliminated
- Only affects Spleef mode

---

### arena.auto-assign

**Type:** `boolean`
**Default:** `true`

Automatically assign available arena to match.

**Example:**
```yaml
arena:
  auto-assign: false  # Manual arena assignment
```

**Notes:**
- `true`: Match auto-assigns to available arena
- `false`: Admin must manually assign arena

---

### match.default-mode

**Type:** `string`
**Default:** `duels`
**Options:** `duels`, `spleef`, `sumo`

Default game mode for new tournaments.

**Example:**
```yaml
match:
  default-mode: spleef  # Default to Spleef
```

---

### match.max-concurrent

**Type:** `integer`
**Default:** `4`
**Range:** `1-16`

Maximum concurrent matches per server.

**Example:**
```yaml
match:
  max-concurrent: 8  # Allow 8 concurrent matches
```

**Notes:**
- Higher values require more arenas
- Each match needs 1 arena
- Recommended: 2-4 for small servers, 8+ for large servers

---

### database.path

**Type:** `string`
**Default:** `emaraleague.db`

SQLite database file path.

**Example:**
```yaml
database:
  path: data/emaraleague.db  # Custom path
```

---

### database MySQL Settings

**Type:** `object`
**Default:** `null` (SQLite)

MySQL connection settings for production servers.

**Example:**
```yaml
database:
  host: localhost
  port: 3306
  database: emaraleague
  username: emaraleague
  password: securepassword
```

**Notes:**
- Uncomment and configure for MySQL
- SQLite is recommended for small-medium servers
- MySQL recommended for large networks

---

### ui.bossbar-countdown

**Type:** `boolean`
**Default:** `true`

Show BossBar countdown during match start.

**Example:**
```yaml
ui:
  bossbar-countdown: false  # Disable BossBar
```

---

### ui.scoreboard

**Type:** `boolean`
**Default:** `true`

Show scoreboard during match.

**Example:**
```yaml
ui:
  scoreboard: false  # Disable scoreboard
```

---

## 💬 messages.yml

### Full Messages Configuration

```yaml
# EmaraLeague Messages
# Customize all plugin messages
# Supports MiniMessage format: https://docs.adventure.kyori.net/minimessage/

# Message prefix
prefix: "<gold>EmaraLeague <red>»</red> "

# Tournament messages
tournament-created: "<green>Tournament '<name>' created successfully!"
tournament-joined: "<green>You joined tournament '<name>'!"
tournament-left: "<green>You left the tournament."
tournament-started: "<green>Tournament '<name>' has started!"
tournament-not-found: "<red>Tournament '<name>' not found."
tournament-already-registered: "<red>You are already registered in this tournament."

# Team messages
team-joined: "<green>You joined team '<team>'!"
team-left: "<green>You left your team."
team-list-header: "<gold>Teams in <tournament>:"
team-list-entry: "  <white>- <team> (<count> players)"

# Arena messages
arena-created: "<green>Arena '<name>' created!"
arena-center-set: "<green>Arena center set for '<name>'."
arena-lobby-set: "<green>Arena lobby set for '<name>'."
arena-deleted: "<green>Arena '<name>' deleted."
arena-not-found: "<red>Arena '<name>' not found."

# Match messages
match-starting: "<yellow>Match starting in <seconds> seconds..."
match-started: "<green>Match started! Fight!"
match-ended: "<yellow>Match ended. Winner: <winner>"
match-countdown: "<gold><seconds>..."

# Error messages
no-permission: "<red>You don't have permission to do that."
invalid-usage: "<red>Invalid usage. Use: <usage>"
player-only: "<red>This command can only be used by players."
tournament-cannot-start: "<red>Tournament needs at least 2 teams with 1 player each to start."

# Help messages
help-header: "<gold>========== EmaraLeague Help =========="
help-entry: "<yellow><command> <gray>- <description>"
help-footer: "<gold>======================================"
```

---

## 🎨 Message Formatting

### MiniMessage Format

Messages use [MiniMessage](https://docs.adventure.kyori.net/minimessage/) format:

```yaml
# Colors
<red>Red text</red>
<green>Green text</green>
<gold>Gold text</gold>
<yellow>Yellow text</yellow>
<white>White text</white>
<gray>Gray text</gray>

# Styles
<bold>Bold text</bold>
<italic>Italic text</italic>
<underlined>Underlined text</underlined>
<strikethrough>Strikethrough text</strikethrough>

# Combined
<gold><bold>Gold bold text</bold></gold>

# Gradients
<gradient:red:blue>Gradient text</gradient>

# Click events
<click:run_command:/el help>Click me</click>

# Hover events
<hover:show_text:'Tooltip'>Hover me</hover>
```

### Placeholders

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `<name>` | Tournament/arena name | `SummerCup` |
| `<team>` | Team name | `Alpha` |
| `<count>` | Player count | `5` |
| `<seconds>` | Countdown seconds | `10` |
| `<winner>` | Winner name | `Alpha` |
| `<mode>` | Game mode | `duels` |
| `<usage>` | Command usage | `/el create <name> <mode>` |
| `<command>` | Command syntax | `/el join` |
| `<description>` | Command description | `Join a tournament` |

---

## 🔄 Reloading Configuration

### In-Game

```
/el reload
```

**Reloads:**
- `config.yml`
- `messages.yml`

### Console

```
plugman reload EmaraLeague
```

**Note:** Not recommended — use `/el reload` instead.

---

## 📝 Notes

- All changes require `/el reload` to take effect
- Invalid YAML will cause plugin to use defaults
- Check console for configuration errors
- Backup configs before major changes
