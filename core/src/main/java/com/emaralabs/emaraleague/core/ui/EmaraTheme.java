package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Centralized color theme for EmaraLeague — Midnight Gold palette.
 * All UI colors must reference this class. Never hardcode hex colors elsewhere.
 *
 * Palette: Dark navy base with gold accents — premium, exclusive, high-stakes.
 */
public final class EmaraTheme {

    private EmaraTheme() {}

    // ── Core palette ────────────────────────────────────────────────
    /** Primary brand color — used for headers, important actions. */
    public static final TextColor PRIMARY = TextColor.color(0xFFD700);

    /** Secondary dark background/base color. */
    public static final TextColor SECONDARY = TextColor.color(0x1A1A2E);

    /** Accent for highlights, links, emphasis. */
    public static final TextColor ACCENT = TextColor.color(0xE94560);

    // ── Semantic status colors ──────────────────────────────────────
    /** Operation completed successfully. */
    public static final TextColor SUCCESS = TextColor.color(0x00D26A);

    /** Operation failed, permission denied, invalid input. */
    public static final TextColor ERROR = TextColor.color(0xFF4444);

    /** Caution, non-critical issues. */
    public static final TextColor WARNING = TextColor.color(0xFFB800);

    /** Informational messages, help text. */
    public static final TextColor INFO = TextColor.color(0x4FC3F7);

    /** Secondary text, disabled options, subtle labels. */
    public static final TextColor MUTED = TextColor.color(0x555555);

    /** Pure white for primary text content. */
    public static final TextColor TEXT = TextColor.color(0xFFFFFF);

    // ── Convenience: status color lookup ────────────────────────────
    public static TextColor status(StatusLevel level) {
        return switch (level) {
            case SUCCESS -> SUCCESS;
            case ERROR -> ERROR;
            case WARNING -> WARNING;
            case INFO -> INFO;
        };
    }

    public enum StatusLevel {
        SUCCESS, ERROR, WARNING, INFO
    }
}
