package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.UUID;

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
            playSound(player, "block.note_block.pling", 1.0f, pitch);
        }
        if (titlesEnabled) {
            Component title = Component.text(String.valueOf(seconds), EmaraTheme.PRIMARY, TextDecoration.BOLD);
            player.showTitle(Title.title(title, Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(800), Duration.ofMillis(200))));
        }
    }

    public void announceMatchStart(Player player) {
        if (soundsEnabled) {
            playSound(player, "entity.ender_dragon.growl", 0.5f, 1.0f);
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
            playSound(player, "entity.player.levelup", 1.0f, 1.5f);
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
            playSound(player, "entity.wither.death", 0.3f, 1.0f);
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
            playSound(player, "entity.generic.hurt", 1.0f, 1.0f);
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
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    playSound(player, "ui.toast.challenge_complete", 1.0f, 1.0f);
                }
            }
        }
    }

    private void playSound(Player player, String soundName, float volume, float pitch) {
        // Use Bukkit's playSound with string name to avoid Sound class initialization in tests
        player.playSound(player.getLocation(), soundName, volume, pitch);
    }
}
