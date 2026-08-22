package com.emaralabs.emaraleague.core.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    // ── Tournament Name ─────────────────────────────────────────────

    @Test
    void validTournamentName() {
        assertTrue(InputValidator.validateTournamentName("SummerCup").isValid());
        assertTrue(InputValidator.validateTournamentName("my_tournament").isValid());
        assertTrue(InputValidator.validateTournamentName("Abc123").isValid());
        assertTrue(InputValidator.validateTournamentName("abc").isValid()); // min 3
        assertTrue(InputValidator.validateTournamentName("a".repeat(24)).isValid()); // max 24
    }

    @Test
    void invalidTournamentName_Null() {
        assertFalse(InputValidator.validateTournamentName(null).isValid());
    }

    @Test
    void invalidTournamentName_Empty() {
        assertFalse(InputValidator.validateTournamentName("").isValid());
        assertFalse(InputValidator.validateTournamentName("   ").isValid());
    }

    @Test
    void invalidTournamentName_TooShort() {
        assertFalse(InputValidator.validateTournamentName("ab").isValid());
        assertFalse(InputValidator.validateTournamentName("a").isValid());
    }

    @Test
    void invalidTournamentName_TooLong() {
        assertFalse(InputValidator.validateTournamentName("a".repeat(25)).isValid());
    }

    @Test
    void invalidTournamentName_StartsWithNumber() {
        assertFalse(InputValidator.validateTournamentName("1tournament").isValid());
    }

    @Test
    void invalidTournamentName_SpecialChars() {
        assertFalse(InputValidator.validateTournamentName("my-tournament").isValid());
        assertFalse(InputValidator.validateTournamentName("my tournament").isValid());
        assertFalse(InputValidator.validateTournamentName("my.tournament").isValid());
        assertFalse(InputValidator.validateTournamentName("my<tournament").isValid());
    }

    @Test
    void tournamentNameErrorMessages() {
        assertNotNull(InputValidator.validateTournamentName(null).getErrorMessage());
        assertNotNull(InputValidator.validateTournamentName("").getErrorMessage());
        assertNotNull(InputValidator.validateTournamentName("ab").getErrorMessage());
        assertNotNull(InputValidator.validateTournamentName("a".repeat(25)).getErrorMessage());
        assertNotNull(InputValidator.validateTournamentName("1abc").getErrorMessage());
    }

    // ── Game Mode ───────────────────────────────────────────────────

    @Test
    void validGameMode() {
        assertTrue(InputValidator.validateGameMode("duels").isValid());
        assertTrue(InputValidator.validateGameMode("spleef").isValid());
        assertTrue(InputValidator.validateGameMode("sumo").isValid());
    }

    @Test
    void invalidGameMode_Null() {
        assertFalse(InputValidator.validateGameMode(null).isValid());
    }

    @Test
    void invalidGameMode_Empty() {
        assertFalse(InputValidator.validateGameMode("").isValid());
    }

    @Test
    void invalidGameMode_Uppercase() {
        assertFalse(InputValidator.validateGameMode("DUELS").isValid());
    }

    @Test
    void invalidGameMode_WithNumbers() {
        assertFalse(InputValidator.validateGameMode("mode123").isValid());
    }

    // ── Player Name ─────────────────────────────────────────────────

    @Test
    void validPlayerName() {
        assertTrue(InputValidator.validatePlayerName("Steve").isValid());
        assertTrue(InputValidator.validatePlayerName("Alex_123").isValid());
        assertTrue(InputValidator.validatePlayerName("a".repeat(16)).isValid()); // max
    }

    @Test
    void invalidPlayerName_TooShort() {
        assertFalse(InputValidator.validatePlayerName("ab").isValid());
    }

    @Test
    void invalidPlayerName_TooLong() {
        assertFalse(InputValidator.validatePlayerName("a".repeat(17)).isValid());
    }

    // ── Arena Name ──────────────────────────────────────────────────

    @Test
    void validArenaName() {
        assertTrue(InputValidator.validateArenaName("Arena_One").isValid());
        assertTrue(InputValidator.validateArenaName("arena-one").isValid());
        assertTrue(InputValidator.validateArenaName("Arena123").isValid());
    }

    @Test
    void invalidArenaName_Null() {
        assertFalse(InputValidator.validateArenaName(null).isValid());
    }
}
