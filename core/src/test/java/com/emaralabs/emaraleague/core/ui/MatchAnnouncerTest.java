package com.emaralabs.emaraleague.core.ui;

import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchAnnouncerTest {

    private MatchAnnouncer announcer;

    @BeforeEach
    void setUp() {
        announcer = new MatchAnnouncer();
    }

    @Test
    void announceCountdown_playsSound() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void announceCountdown_showsTitle() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player).showTitle(any(net.kyori.adventure.title.Title.class));
    }

    @Test
    void announceMatchStart_playsSound() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceMatchStart(player);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void announceMatchStart_showsTitle() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceMatchStart(player);

        verify(player).showTitle(any(net.kyori.adventure.title.Title.class));
    }

    @Test
    void announceVictory_playsSound() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);
        Team team = new Team("Alpha", 1);

        announcer.announceVictory(player, team);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void announceDefeat_playsSound() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);
        Team team = new Team("Beta", 2);

        announcer.announceDefeat(player, team);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void announceElimination_playsSound() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceElimination(player);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void soundsDisabled_noSoundPlayed() {
        announcer.setSoundsEnabled(false);
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player, never()).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void titlesDisabled_noTitleShown() {
        announcer.setTitlesEnabled(false);
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player, never()).showTitle(any(net.kyori.adventure.title.Title.class));
    }

    @Test
    void soundsEnabledByDefault() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player).playSound(any(Location.class), anyString(), anyFloat(), anyFloat());
    }

    @Test
    void titlesEnabledByDefault() {
        Player player = mock(Player.class);
        Location loc = mock(Location.class);
        when(player.getLocation()).thenReturn(loc);

        announcer.announceCountdown(player, 3);

        verify(player).showTitle(any(net.kyori.adventure.title.Title.class));
    }
}
