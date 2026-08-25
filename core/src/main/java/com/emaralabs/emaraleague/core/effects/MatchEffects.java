package com.emaralabs.emaraleague.core.effects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

/**
 * Visual effects for matches — countdown titles and victory fireworks.
 * All effects are configurable via config.yml.
 */
public final class MatchEffects {

    private final Plugin plugin;
    private boolean countdownTitlesEnabled = true;
    private boolean victoryFireworksEnabled = true;

    public MatchEffects(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setCountdownTitlesEnabled(boolean enabled) {
        this.countdownTitlesEnabled = enabled;
    }

    public void setVictoryFireworksEnabled(boolean enabled) {
        this.victoryFireworksEnabled = enabled;
    }

    /**
     * Show a big countdown title to all players (3, 2, 1, FIGHT!).
     */
    public void showCountdownTitle(Iterable<Player> players, int number) {
        if (!countdownTitlesEnabled) {
            return;
        }

        Component titleText;
        Component subtitleText;
        net.kyori.adventure.text.format.TextColor color;

        if (number > 0) {
            color = number == 1
                    ? com.emaralabs.emaraleague.core.ui.EmaraTheme.ERROR
                    : com.emaralabs.emaraleague.core.ui.EmaraTheme.WARNING;
            titleText = Component.text(String.valueOf(number), color, TextDecoration.BOLD);
            subtitleText = Component.text("Get ready...", com.emaralabs.emaraleague.core.ui.EmaraTheme.MUTED);
        } else {
            // FIGHT!
            titleText = Component.text("FIGHT!", com.emaralabs.emaraleague.core.ui.EmaraTheme.PRIMARY, TextDecoration.BOLD);
            subtitleText = Component.text("Good luck!", com.emaralabs.emaraleague.core.ui.EmaraTheme.SUCCESS);
        }

        Title title = Title.title(
                titleText,
                subtitleText,
                Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(800), Duration.ofMillis(300))
        );

        for (Player player : players) {
            if (player != null && player.isOnline()) {
                player.showTitle(title);
                playTickSound(player);
            }
        }
    }

    /**
     * Launch fireworks at a location for the winner.
     */
    public void launchVictoryFireworks(Location location) {
        if (!victoryFireworksEnabled || location == null || location.getWorld() == null) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            // Delay each firework slightly
            int delay = i * 10;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Firework firework = location.getWorld().spawn(location, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.YELLOW, Color.ORANGE)
                        .withFade(Color.RED)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withTrail()
                        .withFlicker()
                        .build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);
            }, delay);
        }
    }

    /**
     * Play a countdown tick sound.
     */
    private void playTickSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }

    /**
     * Play fight start sound.
     */
    public void playFightSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
    }
}
