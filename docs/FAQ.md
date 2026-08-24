# EmaraLeague — Frequently Asked Questions

> **Version:** 1.0.1
> **Last Updated:** August 2026

---

## 🎮 General Questions

### Q: What is EmaraLeague?

EmaraLeague is a premium Paper plugin that lets you create and manage tournaments on your Minecraft server. It includes bracket management, multiple game modes, arena management, and live scoring.

### Q: What server types are supported?

**Paper 1.21+ only.** Spigot, Bukkit, and other server types are not supported.

### Q: What Minecraft versions are supported?

Minecraft 1.21 and above. Older versions are not supported.

### Q: Is EmaraLeague free?

No, EmaraLeague is a premium plugin. It costs $19.99 (one-time purchase).

### Q: Do I need to pay for updates?

No, all v1.x updates are free. You only pay once for the plugin.

---

## 🛠️ Installation & Setup

### Q: How do I install EmaraLeague?

1. Purchase from BuiltByBit or SpigotMC
2. Download the JAR file
3. Copy to `plugins/` folder
4. Restart server
5. Check console for `EmaraLeague enabled`

### Q: How do I create my first tournament?

```
/el arena create MyArena
/el arena setcenter MyArena
/el arena setlobby MyArena
/el create MyTournament duels
/el join MyTournament
/el start MyTournament
```

### Q: How many arenas do I need?

- **1 arena:** 1 match at a time
- **2-4 arenas:** Multiple concurrent matches
- **8+ arenas:** Large tournaments

### Q: Can I use existing worlds as arenas?

Yes, you can use any world. Just stand in the world and use `/el arena setcenter` and `/el arena setlobby`.

---

## 🎯 Game Modes

### Q: What game modes are included?

|| Mode | Description |
||------|-------------|
|| **Duels** | 1v1 PvP with kill elimination |
|| **Spleef** | Block breaking with fall elimination |
|| **Sumo** | Knockback arena with ring-out elimination |
|| **TNTRun** | Blocks disappear under your feet (coming in v1.1) |
|| **Parkour** | Race to finish with checkpoints (coming in v1.1) |
|| **Capture The Flag** | Team-based flag capture (coming in v1.1) |

### Q: Can I add custom game modes?

Yes, custom game modes can be added via the API. See [API Documentation](API.md) for details.

### Q: Are more game modes coming?

Yes, TNTRun, Parkour, and Capture The Flag are in progress for v1.1 (estimated September 2026).

---

## ⚙️ Configuration

### Q: How do I change the countdown duration?

Edit `config.yml`:
```yaml
countdown-seconds: 15  # 15 seconds
```

Then run `/el reload`.

### Q: How do I customize messages?

Edit `messages.yml`:
```yaml
tournament-created: "<green>Custom message here!"
```

Then run `/el reload`.

### Q: How do I disable the scoreboard?

Edit `config.yml`:
```yaml
ui:
  scoreboard: false
```

Then run `/el reload`.

### Q: How do I use MySQL instead of SQLite?

Edit `config.yml`:
```yaml
database:
  host: localhost
  port: 3306
  database: emaraleague
  username: root
  password: password
```

Then run `/el reload`.

---

## 🔧 Troubleshooting

### Q: Plugin won't enable. What do I do?

1. Check server version (Paper 1.21+ required)
2. Check Java version (Java 21+ required)
3. Check console for error messages
4. Join Discord for support

### Q: Players can't join tournaments. Why?

Common causes:
- Tournament doesn't exist (check spelling)
- Player already registered
- Tournament not in REGISTRATION state
- Permission issue (check `emaraleague.play`)

### Q: Tournament won't start. Why?

Common causes:
- Not enough teams (need 2+)
- Teams have no players (need 1+ per team)
- Tournament already started
- Permission issue (check `emaraleague.admin`)

### Q: Players aren't teleporting to arena. Why?

Common causes:
- Arena center not set
- Arena lobby not set
- Arena not in LOBBY state
- Player not in match

### Q: Scoreboard not showing. Why?

Common causes:
- `ui.scoreboard: false` in config
- Player not in match
- Scoreboard disabled by another plugin

---

## 🔒 Permissions

### Q: How do I give players permission to join tournaments?

Use a permission plugin (LuckPerms, PermissionsEx):
```bash
lp group default permission set emaraleague.play true
```

### Q: How do I give staff permission to create tournaments?

```bash
lp group moderator permission set emaraleague.create true
```

### Q: How do I give admins full access?

```bash
lp group admin permission set emaraleague.* true
```

---

## 💰 Pricing & Licensing

### Q: How much does EmaraLeague cost?

$19.99 one-time purchase. No subscription, no recurring fees.

### Q: Do I get free updates?

Yes, all v1.x updates are free. v2.0 may require a paid upgrade.

### Q: Can I get a refund?

Refund policy varies by marketplace:
- **BuiltByBit:** 24-hour refund policy
- **SpigotMC:** Contact support

### Q: Can I share the plugin with friends?

No, the license is per-server. Each server needs its own license.

### Q: Can I use it on multiple servers?

One license covers one server. For multiple servers, contact us for volume licensing.

---

## 📞 Support

### Q: How do I get support?

1. **Discord:** [discord.gg/emaralabs](https://discord.gg/emaralabs) (fastest)
2. **Documentation:** [docs.emaralabs.com](https://docs.emaralabs.com)
3. **GitHub:** [github.com/EmaraLabs/emaraleague/issues](https://github.com/EmaraLabs/emaraleague/issues)

### Q: What information should I include in a bug report?

- Server version (`/version`)
- Plugin version (`/version EmaraLeague`)
- Error message (if any)
- Steps to reproduce
- Expected vs actual behavior

### Q: How do I report a bug?

1. Join Discord
2. Go to #bug-reports
3. Fill out the bug report template
4. Include all required information

---

## 🆕 Updates & Roadmap

### Q: When is the next update?

Updates are released monthly. Check Discord for announcements.

### Q: What's coming in v1.1?

- **GUI Editor** — In-game inventory GUI for arena and tournament setup
- **Multi-language** — English, Spanish, Portuguese, Russian, Chinese
- **More game modes** — TNTRun, Parkour, Capture The Flag
- **Spectator improvements** — `/el spectate off` command

### Q: How do I request a feature?

1. Join Discord
2. Go to #suggestions
3. Describe your feature
4. Community votes on features

---

## 📚 Additional Resources

- **Setup Guide:** [SETUP.md](SETUP.md)
- **Command Reference:** [COMMANDS.md](COMMANDS.md)
- **Permission Reference:** [PERMISSIONS.md](PERMISSIONS.md)
- **Configuration Guide:** [CONFIGURATION.md](CONFIGURATION.md)
- **API Documentation:** [API.md](API.md) (for developers)
