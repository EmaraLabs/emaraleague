package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageRegistryTest {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    @Test
    void prefixMessageContainsBrand() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("prefix");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("EmaraLeague"));
        assertTrue(plain.contains("»"));
    }

    @Test
    void tournamentCreatedMessageWithPlaceholder() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("tournament-created", Map.of("name", "SummerCup"));
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("SummerCup"));
        assertTrue(plain.contains("created successfully"));
    }

    @Test
    void noPermissionMessageWithPlaceholder() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("no-permission", Map.of("permission", "emaraleague.admin"));
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("emaraleague.admin"));
        assertTrue(plain.contains("permission"));
    }

    @Test
    void invalidGameModeMessageWithPlaceholders() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("invalid-game-mode", Map.of(
                "mode", "unknown",
                "modes", "duels, spleef, sumo"));
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("unknown"));
        assertTrue(plain.contains("duels, spleef, sumo"));
    }

    @Test
    void unknownCommandMessage() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("unknown-command");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("Unknown command"));
    }

    @Test
    void reloadSuccessMessage() {
        MessageRegistry registry = createRegistry();
        Component msg = registry.get("reload-success");
        String plain = PLAIN.serialize(msg);
        assertTrue(plain.contains("reloaded"));
    }

    @Test
    void allDefaultKeysExist() {
        MessageRegistry registry = createRegistry();
        assertTrue(registry.getKeys().contains("prefix"));
        assertTrue(registry.getKeys().contains("no-permission"));
        assertTrue(registry.getKeys().contains("invalid-usage"));
        assertTrue(registry.getKeys().contains("unknown-command"));
        assertTrue(registry.getKeys().contains("tournament-created"));
        assertTrue(registry.getKeys().contains("tournament-joined"));
        assertTrue(registry.getKeys().contains("tournament-left"));
        assertTrue(registry.getKeys().contains("tournament-started"));
        assertTrue(registry.getKeys().contains("tournament-info"));
        assertTrue(registry.getKeys().contains("tournament-not-found"));
        assertTrue(registry.getKeys().contains("invalid-tournament-name"));
        assertTrue(registry.getKeys().contains("invalid-game-mode"));
        assertTrue(registry.getKeys().contains("help-header"));
        assertTrue(registry.getKeys().contains("help-create"));
        assertTrue(registry.getKeys().contains("help-join"));
        assertTrue(registry.getKeys().contains("help-leave"));
        assertTrue(registry.getKeys().contains("help-start"));
        assertTrue(registry.getKeys().contains("help-info"));
        assertTrue(registry.getKeys().contains("help-help"));
        assertTrue(registry.getKeys().contains("player-only"));
        assertTrue(registry.getKeys().contains("reload-success"));
    }

    private MessageRegistry createRegistry() {
        org.bukkit.plugin.Plugin mockPlugin = org.mockito.Mockito.mock(org.bukkit.plugin.Plugin.class);
        java.io.File tempDir = new java.io.File("build/tmp/test-msg-registry");
        tempDir.mkdirs();
        org.mockito.Mockito.when(mockPlugin.getDataFolder()).thenReturn(tempDir);
        org.mockito.Mockito.when(mockPlugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        return new MessageRegistry(mockPlugin);
    }
}
