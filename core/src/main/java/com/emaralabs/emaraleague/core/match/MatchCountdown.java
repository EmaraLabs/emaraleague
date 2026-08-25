package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.scheduler.EmaraScheduler;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MatchCountdown {

    private final EmaraScheduler scheduler;
    private final MessageRegistry messages;
    private final List<Player> players = new ArrayList<>();
    private int remainingSeconds;
    private int totalSeconds;
    private boolean running;
    private BossBar bossBar;
    private com.emaralabs.emaraleague.core.effects.MatchEffects effects;

    public MatchCountdown(EmaraScheduler scheduler, MessageRegistry messages) {
        this.scheduler = scheduler;
        this.messages = messages;
        this.running = false;
        this.remainingSeconds = 0;
    }

    public void setEffects(com.emaralabs.emaraleague.core.effects.MatchEffects effects) {
        this.effects = effects;
    }

    public void startCountdown(Match match, int seconds, Runnable onComplete) {
        this.remainingSeconds = seconds;
        this.totalSeconds = seconds;
        this.running = true;

        bossBar = BossBar.bossBar(
                Component.text("Match starting in " + seconds + "s"),
                1.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS
        );

        for (Player player : players) {
            player.showBossBar(bossBar);
        }

        scheduler.runRepeating(() -> {
            if (remainingSeconds <= 0) {
                running = false;
                hideBossBar();
                // Show FIGHT! title
                if (effects != null) {
                    effects.showCountdownTitle(players, 0);
                }
                onComplete.run();
                return;
            }
            tick();
        }, 0, 20);
    }

    public void tick() {
        if (remainingSeconds <= 0) {
            running = false;
            hideBossBar();
            return;
        }

        // Show countdown title for last 3 seconds + FIGHT!
        if (effects != null && remainingSeconds <= 3) {
            effects.showCountdownTitle(players, remainingSeconds);
        }

        remainingSeconds--;
        updateBossBar();
    }

    private void updateBossBar() {
        if (bossBar == null) {
            return;
        }
        float progress = (float) remainingSeconds / totalSeconds;
        bossBar.progress(progress);
        bossBar.name(Component.text("Match starting in " + remainingSeconds + "s"));
        if (remainingSeconds <= 3) {
            bossBar.color(BossBar.Color.RED);
        } else if (remainingSeconds <= 5) {
            bossBar.color(BossBar.Color.YELLOW);
        }
    }

    private void hideBossBar() {
        if (bossBar == null) {
            return;
        }
        for (Player player : players) {
            player.hideBossBar(bossBar);
        }
        bossBar = null;
    }

    public void cancel() {
        running = false;
        remainingSeconds = 0;
        hideBossBar();
    }

    public boolean isRunning() {
        return running;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void addPlayer(Player player) {
        players.add(player);
        if (bossBar != null) {
            player.showBossBar(bossBar);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    public void clearPlayers() {
        players.clear();
    }

    public int getPlayerCount() {
        return players.size();
    }
}
