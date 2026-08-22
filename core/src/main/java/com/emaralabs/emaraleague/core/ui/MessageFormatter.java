package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Message templates and formatting utilities for EmaraLeague.
 * Every player-facing message flows through here — consistent prefix,
 * colors, and structure.
 */
public final class MessageFormatter {

    private MessageFormatter() {}

    /** The branded prefix: gold "EmaraLeague" + accent arrow. */
    private static final Component PREFIX = Component.text()
            .append(Component.text("EmaraLeague", EmaraTheme.PRIMARY, TextDecoration.BOLD))
            .append(Component.text(" » ", EmaraTheme.ACCENT))
            .build();

    /** Formats a message with the branded prefix and semantic color. */
    public static Component format(String message, EmaraTheme.StatusLevel level) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text(message, EmaraTheme.status(level)))
                .build();
    }

    /** Success message — green with prefix. */
    public static Component success(String message) {
        return format(message, EmaraTheme.StatusLevel.SUCCESS);
    }

    /** Error message — red with prefix. */
    public static Component error(String message) {
        return format(message, EmaraTheme.StatusLevel.ERROR);
    }

    /** Warning message — amber with prefix. */
    public static Component warning(String message) {
        return format(message, EmaraTheme.StatusLevel.WARNING);
    }

    /** Info message — light blue with prefix. */
    public static Component info(String message) {
        return format(message, EmaraTheme.StatusLevel.INFO);
    }

    /** No-permission message — red with actionable hint. */
    public static Component noPermission(String permission) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text("You don't have permission ", EmaraTheme.ERROR))
                .append(Component.text("(", EmaraTheme.MUTED))
                .append(Component.text(permission, EmaraTheme.MUTED))
                .append(Component.text(")", EmaraTheme.MUTED))
                .build();
    }

    /** Invalid usage — red with correct usage. */
    public static Component invalidUsage(String usage) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text("Invalid usage. ", EmaraTheme.ERROR))
                .append(Component.text("Usage: ", EmaraTheme.MUTED))
                .append(Component.text(usage, EmaraTheme.WARNING))
                .build();
    }

    /** Header line for menus / help — bold gold with underline. */
    public static Component header(String title) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text(title, EmaraTheme.PRIMARY, TextDecoration.BOLD, TextDecoration.UNDERLINED))
                .build();
    }

    /** Sub-header / section label — bold accent. */
    public static Component section(String label) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text(label, EmaraTheme.ACCENT, TextDecoration.BOLD))
                .build();
    }

    /** Muted / secondary text line. */
    public static Component muted(String message) {
        return Component.text()
                .append(PREFIX)
                .append(Component.text(message, EmaraTheme.MUTED))
                .build();
    }

    /** Raw prefix component for custom compositions. */
    public static Component prefix() {
        return PREFIX;
    }
}
