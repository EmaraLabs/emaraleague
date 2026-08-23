# EmaraLeague — Permission Reference

> **Version:** 1.0.0
> **Last Updated:** August 2026

---

## 📋 Permission Nodes

| Permission | Default | Description |
|------------|---------|-------------|
| `emaraleague.use` | true | Basic plugin access |
| `emaraleague.play` | true | Join and play tournaments |
| `emaraleague.create` | op | Create tournaments |
| `emaraleague.admin` | op | Admin commands |
| `emaraleague.reload` | op | Reload configuration |

---

## 🔍 Detailed Permissions

### `emaraleague.use`

**Default:** `true` (all players)

**Allows:**
- `/el help` — View help
- `/el info` — View tournament info
- `/el team list` — List teams
- `/el arena list` — List arenas

**Use case:** Basic read-only access for all players.

---

### `emaraleague.play`

**Default:** `true` (all players)

**Allows:**
- `/el join` — Join tournaments
- `/el leave` — Leave tournaments
- `/el team join` — Join specific team
- `/el team leave` — Leave team

**Use case:** Players who can participate in tournaments.

**Recommended setup:**
```yaml
# LuckPerms example
lp group default permission set emaraleague.play true
lp group vip permission set emaraleague.play true
lp group moderator permission set emaraleague.play true
```

---

### `emaraleague.create`

**Default:** `op` (operators only)

**Allows:**
- `/el create` — Create tournaments

**Use case:** Server staff who can create tournaments.

**Recommended setup:**
```yaml
# LuckPerms example
lp group admin permission set emaraleague.create true
lp group owner permission set emaraleague.create true
```

---

### `emaraleague.admin`

**Default:** `op` (operators only)

**Allows:**
- `/el start` — Start tournaments
- `/el arena create` — Create arenas
- `/el arena setcenter` — Set arena center
- `/el arena setlobby` — Set arena lobby
- `/el arena delete` — Delete arenas

**Use case:** Server administrators who manage tournaments and arenas.

**Recommended setup:**
```yaml
# LuckPerms example
lp group admin permission set emaraleague.admin true
lp group owner permission set emaraleague.admin true
```

---

### `emaraleague.reload`

**Default:** `op` (operators only)

**Allows:**
- `/el reload` — Reload configuration

**Use case:** Server administrators who need to reload config without restart.

**Recommended setup:**
```yaml
# LuckPerms example
lp group admin permission set emaraleague.reload true
lp group owner permission set emaraleague.reload true
```

---

## 🛠️ Permission Plugin Examples

### LuckPerms

```bash
# Create groups
lp creategroup player
lp creategroup vip
lp creategroup moderator
lp creategroup admin

# Set permissions
lp group player permission set emaraleague.use true
lp group player permission set emaraleague.play true

lp group vip permission set emaraleague.use true
lp group vip permission set emaraleague.play true

lp group moderator permission set emaraleague.use true
lp group moderator permission set emaraleague.play true
lp group moderator permission set emaraleague.create true

lp group admin permission set emaraleague.use true
lp group admin permission set emaraleague.play true
lp group admin permission set emaraleague.create true
lp group admin permission set emaraleague.admin true
lp group admin permission set emaraleague.reload true

# Assign players to groups
lp user Notch parent set admin
```

### PermissionsEx

```yaml
# config.yml
groups:
  player:
    permissions:
    - emaraleague.use
    - emaraleague.play

  vip:
    inheritance:
    - player
    permissions:
    - emaraleague.use
    - emaraleague.play

  moderator:
    inheritance:
    - vip
    permissions:
    - emaraleague.create

  admin:
    inheritance:
    - moderator
    permissions:
    - emaraleague.admin
    - emaraleague.reload
```

---

## 🎯 Recommended Setups

### Public Server (Open Tournaments)

```yaml
# All players can join tournaments
emaraleague.use: true
emaraleague.play: true

# Only staff can create/manage
emaraleague.create: op
emaraleague.admin: op
emaraleague.reload: op
```

### Private Server (Restricted Tournaments)

```yaml
# Only VIP can play
emaraleague.use: true
emaraleague.play: vip

# Only admin can create/manage
emaraleague.create: admin
emaraleague.admin: admin
emaraleague.reload: admin
```

### Competitive Server (Ranked Tournaments)

```yaml
# All players can join ranked
emaraleague.use: true
emaraleague.play: true

# Only ranked admins can create
emaraleague.create: ranked.admin
emaraleague.admin: ranked.admin
emaraleague.reload: ranked.admin
```

---

## 🔒 Wildcard Permissions

| Wildcard | Includes |
|----------|----------|
| `emaraleague.*` | All permissions |
| `emaraleague.admin.*` | create, admin, reload |
| `emaraleague.play.*` | play, team join/leave |

**Example:**
```yaml
# Give all permissions to owner
lp group owner permission set emaraleague.* true
```

---

## 📝 Notes

- Permissions are checked on command execution
- Missing permission shows "You don't have permission" message
- Permissions can be changed without restart (requires `/el reload`)
- Use a permission plugin (LuckPerms, PermissionsEx) for group management
