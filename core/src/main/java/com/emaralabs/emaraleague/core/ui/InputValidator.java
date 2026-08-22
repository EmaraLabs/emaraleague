package com.emaralabs.emaraleague.core.ui;

import java.util.regex.Pattern;

/**
 * Input validation for all user-supplied data.
 * Centralized — every command validates through here before processing.
 */
public final class InputValidator {

    private InputValidator() {}

    /** Tournament name: 3-24 chars, alphanumeric + underscores, must start with letter. */
    private static final Pattern TOURNAMENT_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,23}$");

    /** Player name: 3-16 chars, alphanumeric + underscores. */
    private static final Pattern PLAYER_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    /** Arena name: 3-32 chars, alphanumeric + underscores + hyphens. */
    private static final Pattern ARENA_NAME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{2,31}$");

    /** Game mode: lowercase alphabetic, 2-20 chars. */
    private static final Pattern GAME_MODE = Pattern.compile("^[a-z]{2,20}$");

    public static ValidationResult validateTournamentName(String name) {
        if (name == null || name.isBlank()) {
            return ValidationResult.error("Tournament name cannot be empty.");
        }
        if (name.length() < 3) {
            return ValidationResult.error("Tournament name must be at least 3 characters.");
        }
        if (name.length() > 24) {
            return ValidationResult.error("Tournament name must be at most 24 characters.");
        }
        if (!TOURNAMENT_NAME.matcher(name).matches()) {
            return ValidationResult.error(
                "Tournament name must start with a letter and contain only letters, numbers, and underscores.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePlayerName(String name) {
        if (name == null || name.isBlank()) {
            return ValidationResult.error("Player name cannot be empty.");
        }
        if (!PLAYER_NAME.matcher(name).matches()) {
            return ValidationResult.error("Invalid player name format.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateArenaName(String name) {
        if (name == null || name.isBlank()) {
            return ValidationResult.error("Arena name cannot be empty.");
        }
        if (!ARENA_NAME.matcher(name).matches()) {
            return ValidationResult.error("Arena name must be 3-32 characters, alphanumeric, underscores, and hyphens only.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateGameMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return ValidationResult.error("Game mode cannot be empty.");
        }
        if (!GAME_MODE.matcher(mode).matches()) {
            return ValidationResult.error("Game mode must be lowercase alphabetic (e.g., duels, spleef).");
        }
        return ValidationResult.ok();
    }

    /** Simple validation result wrapper. */
    public record ValidationResult(boolean valid, String errorMessage) {
        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
