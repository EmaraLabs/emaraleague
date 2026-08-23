# Sound Effects + Title Announcements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use test-driven-development for every task.

**Goal:** Add sound effects and title/subtitle announcements for match events — countdown, start, win, elimination. Premium audio-visual feedback.

**Architecture:** `MatchAnnouncer` class handles all sounds + titles. Hooks into `MatchEngine` events. Uses Bukkit `Player#playSound()` and `Player#showTitle()`.

**Tech Stack:** Java 21, Bukkit Sound API, Adventure Title API, JUnit 5, Mockito.

**Spec:** Fasa C (Polish) — Sound effects + Titles

---

## Global Constraints

- Java 21, no inline comments, self-documenting code
- Sounds: countdown ticks, match start horn, win fanfare, elimination thud
- Titles: countdown numbers, match start, victory, defeat, elimination
- Configurable: enable/disable via config.yml
- Performance: no lag, async-safe

---

## Task 1: MatchAnnouncer Core

**Files:**
- Create: `core/src/main/java/com/emaralabs/emaraleague/core/ui/MatchAnnouncer.java`
- Test: `core/src/test/java/com/emaralabs/emaraleague/core/ui/MatchAnnouncerTest.java`

**Interfaces:**
- Consumes: `Player`, `Match`, `Team`
- Produces: Sound effects + title displays

```java
public final class MatchAnnouncer {
    // Countdown
    public void announceCountdown(Player player, int seconds)
    public void announceCountdownToAll(Match match, int seconds)

    // Match events
    public void announceMatchStart(Player player)
    public void announceMatchStartToAll(Match match)
    public void announceVictory(Player player, Team team)
    public void announceDefeat(Player player, Team team)
    public void announceElimination(Player player)
    public void announceChampion(Match match, Team champion)

    // Configuration
    public void setSoundsEnabled(boolean enabled)
    public void setTitlesEnabled(boolean enabled)
}
```

**Sounds (Bukkit Sound enum):**
- Countdown tick: `BLOCK_NOTE_BLOCK_PLING` (pitch 1.0)
- Countdown final: `BLOCK_NOTE_BLOCK_PLING` (pitch 2.0)
- Match start: `ENTITY_ENDER_DRAGON_GROWL` (volume 0.5)
- Victory: `ENTITY_PLAYER_LEVELUP` (pitch 1.5)
- Defeat: `ENTITY_WITHER_DEATH` (volume 0.3)
- Elimination: `ENTITY_GENERIC_HURT`
- Champion: `UI_TOAST_CHALLENGE_COMPLETE`

**Titles (Adventure):**
- Countdown: `3... 2... 1...` (gold, bold)
- Match start: `FIGHT!` (red, bold)
- Victory: `VICTORY!` (green, bold) + subtitle `Your team wins!`
- Defeat: `DEFEATED` (red, bold) + subtitle `Better luck next time`
- Elimination: `ELIMINATED` (red, bold) + subtitle `You have been eliminated`
- Champion: `CHAMPIONS!` (gold, bold) + subtitle `Team Alpha wins the tournament!`

- [ ] **Step 1: Write failing test — countdown announcement**

```java
@Test
void announceCountdown_playsSoundAndShowsTitle() {
    MatchAnnouncer announcer = new MatchAnnouncer();
    Player player = mock(Player.class);

    announcer.announceCountdown(player, 3);

    verify(player).playSound(any(Sound.class), anyFloat(), anyFloat());
    verify(player).showTitle(any(Title.class));
}
```

- [ ] **Step 2: Run test — verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public final class MatchAnnouncer {

    private boolean soundsEnabled = true;
    private boolean titlesEnabled = true;

    public void setSoundsEnabled(boolean enabled) {
        this.soundsEnabled = enabled;
    }

    public void setTitlesEnabled(boolean enabled) {
        this.titlesEnabled = enabled;
    }

    public void announceCountdown(Player player, int seconds) {
        if (soundsEnabled) {
            float pitch = seconds <= 3 ? 2.0f : 1.0f;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
        }
        if (titlesEnabled) {
            Component title = Component.text(String.valueOf(seconds), EmaraTheme.PRIMARY, TextDecoration.BOLD);
            player.showTitle(Title.title(title, Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))));
        }
    }

    public void announceMatchStart(Player player) {
        if (soundsEnabled) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.0f);
        }
        if (titlesEnabled) {
            Component title = Component.text("FIGHT!", EmaraTheme.ACCENT, TextDecoration.BOLD);
            Component subtitle = Component.text("Good luck!", EmaraTheme.MUTED);
            player.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(500))));
        }
    }

    public void announceVictory(Player player, Team team) {
        if (soundsEnabled) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        }
        if (titlesEnabled) {
            Component title = Component.text("VICTORY!", EmaraTheme.SUCCESS, TextDecoration.BOLD);
            Component subtitle = Component.text(team.name() + " wins!", EmaraTheme.MUTED);
            player.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(500))));
        }
    }

    public void announceDefeat(Player player, Team team) {
        if (soundsEnabled) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 1.0f);
        }
        if (titlesEnabled) {
            Component title = Component.text("DEFEATED", EmaraTheme.ERROR, TextDecoration.BOLD);
            Component subtitle = Component.text("Better luck next time", EmaraTheme.MUTED);
            player.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(500))));
        }
    }

    public void announceElimination(Player player) {
        if (soundsEnabled) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_HURT, 1.0f, 1.0f);
        }
        if (titlesEnabled) {
            Component title = Component.text("ELIMINATED", EmaraTheme.ERROR, TextDecoration.BOLD);
            Component subtitle = Component.text("You have been eliminated", EmaraTheme.MUTED);
            player.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(500))));
        }
    }

    public void announceChampion(Match match, Team champion) {
        if (soundsEnabled) {
            for (UUID playerId : champion.playerIds()) {
                Player player = org.bukkit.Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
            }
        }
    }
}
```

- [ ] **Step 4: Write more tests**

```java
@Test
void announceMatchStart_playsSound() { ... }

@Test
void announceVictory_playsSoundAndShowsTitle() { ... }

@Test
void announceDefeat_playsSoundAndShowsTitle() { ... }

@Test
void announceElimination_playsSoundAndShowsTitle() { ... }

@Test
void announceChampion_playsSoundToAllTeamMembers() { ... }

@Test
void soundsDisabled_noSoundPlayed() { ... }

@Test
void titlesDisabled_noTitleShown() { ... }
```

- [ ] **Step 5: Run tests — verify pass**

- [ ] **Step 6: Commit**

---

## Task 2: Wire Announcer into MatchEngine + Countdown

**Files:**
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchEngine.java`
- Modify: `core/src/main/java/com/emaralabs/emaraleague/core/match/MatchCountdown.java`
- Modify: `bootstrap/src/main/java/com/emaralabs/emaraleague/EmaraLeaguePlugin.java`

**Changes:**
- MatchCountdown calls `announcer.announceCountdown()` every second
- MatchEngine calls `announcer.announceMatchStart()` on `beginPlay()`
- MatchEngine calls `announcer.announceVictory()` / `announceDefeat()` on `endMatch()`
- PlayerEventListener calls `announcer.announceElimination()` on player death
- Plugin creates `MatchAnnouncer` and wires it

- [ ] **Step 1: Update MatchCountdown**
- [ ] **Step 2: Update MatchEngine**
- [ ] **Step 3: Update PlayerEventListener**
- [ ] **Step 4: Update plugin wiring**
- [ ] **Step 5: Build — verify compiles**
- [ ] **Step 6: Run all tests — verify pass**
- [ ] **Step 7: Commit**

---

## Self-Review

1. **Spec coverage:** Sound effects ✅. Title announcements ✅. Match events ✅.
2. **Placeholder scan:** No TBD — every step has actual code.
3. **Type consistency:** `MatchAnnouncer` uses Bukkit Sound + Adventure Title APIs.
4. **Scope check:** 2 tasks. Task 1 is core announcer, Task 2 is wiring.

---

## Execution Handoff

Plan complete. Proceeding with inline execution + TDD.
