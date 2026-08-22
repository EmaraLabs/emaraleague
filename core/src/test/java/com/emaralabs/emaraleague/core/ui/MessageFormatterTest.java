package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageFormatterTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    void prefixContainsBrandName() {
        String plain = PLAIN.serialize(MessageFormatter.prefix());
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("»"));
    }

    @Test
    void successMessage() {
        Component msg = MessageFormatter.success("Test success");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Test success"));
    }

    @Test
    void errorMessage() {
        Component msg = MessageFormatter.error("Test error");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Test error"));
    }

    @Test
    void warningMessage() {
        Component msg = MessageFormatter.warning("Test warning");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Test warning"));
    }

    @Test
    void infoMessage() {
        Component msg = MessageFormatter.info("Test info");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Test info"));
    }

    @Test
    void noPermissionMessage() {
        Component msg = MessageFormatter.noPermission("emaraleague.admin");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("emaraleague.admin"));
        assertTrue(plain.contains("permission"));
    }

    @Test
    void invalidUsageMessage() {
        Component msg = MessageFormatter.invalidUsage("/emaraleague create <name> <mode>");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("/emaraleague create"));
        assertTrue(plain.contains("Usage:"));
    }

    @Test
    void headerMessage() {
        Component msg = MessageFormatter.header("Commands");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Commands"));
    }

    @Test
    void mutedMessage() {
        Component msg = MessageFormatter.muted("Subtle text");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("Subtle text"));
    }
}
