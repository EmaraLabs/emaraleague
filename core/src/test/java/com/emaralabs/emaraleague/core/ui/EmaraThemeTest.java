package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmaraThemeTest {

    @Test
    void primaryColorIsGold() {
        assertEquals(TextColor.color(0xFFD700), EmaraTheme.PRIMARY);
    }

    @Test
    void secondaryColorIsDarkNavy() {
        assertEquals(TextColor.color(0x1A1A2E), EmaraTheme.SECONDARY);
    }

    @Test
    void accentColorIsCrimson() {
        assertEquals(TextColor.color(0xE94560), EmaraTheme.ACCENT);
    }

    @Test
    void successColorIsEmerald() {
        assertEquals(TextColor.color(0x00D26A), EmaraTheme.SUCCESS);
    }

    @Test
    void errorColorIsRed() {
        assertEquals(TextColor.color(0xFF4444), EmaraTheme.ERROR);
    }

    @Test
    void warningColorIsAmber() {
        assertEquals(TextColor.color(0xFFB800), EmaraTheme.WARNING);
    }

    @Test
    void infoColorIsLightBlue() {
        assertEquals(TextColor.color(0x4FC3F7), EmaraTheme.INFO);
    }

    @Test
    void mutedColorIsGray() {
        assertEquals(TextColor.color(0x555555), EmaraTheme.MUTED);
    }

    @Test
    void statusColorMapping() {
        assertEquals(EmaraTheme.SUCCESS, EmaraTheme.status(EmaraTheme.StatusLevel.SUCCESS));
        assertEquals(EmaraTheme.ERROR, EmaraTheme.status(EmaraTheme.StatusLevel.ERROR));
        assertEquals(EmaraTheme.WARNING, EmaraTheme.status(EmaraTheme.StatusLevel.WARNING));
        assertEquals(EmaraTheme.INFO, EmaraTheme.status(EmaraTheme.StatusLevel.INFO));
    }
}
