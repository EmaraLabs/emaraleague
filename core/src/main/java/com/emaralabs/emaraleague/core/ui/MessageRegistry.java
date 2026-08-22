package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class MessageRegistry {

    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> messages = new HashMap<>();

    public MessageRegistry(Plugin plugin) {
        this.plugin = plugin;
        loadDefaults();
        loadFromFile();
    }

    private void loadDefaults() {
        messages.put("prefix", "<bold><#FFD700>EmaraLeague</#FFD700></bold> <#E94560>»</#E94560> ");
        messages.put("no-permission", "<#FF4444>You don't have permission <#555555>(</#555555><#555555><permission></#555555><#555555>)</#555555>");
        messages.put("invalid-usage", "<#FF4444>Invalid usage. <#555555>Usage:</#555555> <#FFB800><usage></#FFB800>");
        messages.put("unknown-command", "<#FF4444>Unknown command. Type <#FFB800>/emaraleague help</#FFB800> for a list of commands.");
        messages.put("tournament-created", "<#00D26A>Tournament '</#00D26A><#FFFFFF><name></#FFFFFF><#00D26A>' created successfully.</#00D26A>");
        messages.put("tournament-joined", "<#00D26A>You have joined tournament '</#00D26A><#FFFFFF><name></#FFFFFF><#00D26A>'.</#00D26A>");
        messages.put("tournament-left", "<#00D26A>You have left the tournament.</#00D26A>");
        messages.put("tournament-started", "<#00D26A>Tournament '</#00D26A><#FFFFFF><name></#FFFFFF><#00D26A>' has started!</#00D26A>");
        messages.put("tournament-info", "<#4FC3F7>Tournament: </#4FC3F7><#FFFFFF><name></#FFFFFF> <#555555>|</#555555> <#4FC3F7>Mode: </#4FC3F7><#FFFFFF><mode></#FFFFFF> <#555555>|</#555555> <#4FC3F7>Status: </#4FC3F7><#FFFFFF><status></#FFFFFF>");
        messages.put("tournament-not-found", "<#FF4444>Tournament '</#FF4444><#FFFFFF><name></#FFFFFF><#FF4444>' not found.</#FF4444>");
        messages.put("invalid-tournament-name", "<#FF4444>Tournament name must be 3-24 characters, alphanumeric and underscores only.</#FF4444>");
        messages.put("invalid-game-mode", "<#FF4444>Unknown game mode '</#FF4444><#FFFFFF><mode></#FFFFFF><#FF4444>'. Available: <#FFB800><modes></#FFB800>");
        messages.put("help-header", "<bold><#FFD700>EmaraLeague Commands</#FFD700></bold>");
        messages.put("help-create", "<#FFB800>/emaraleague create <name> <mode></#FFB800> <#555555>—</#555555> <#4FC3F7>Create a new tournament</#4FC3F7>");
        messages.put("help-join", "<#FFB800>/emaraleague join <tournament></#FFB800> <#555555>—</#555555> <#4FC3F7>Join a tournament</#4FC3F7>");
        messages.put("help-leave", "<#FFB800>/emaraleague leave</#FFB800> <#555555>—</#555555> <#4FC3F7>Leave your current tournament</#4FC3F7>");
        messages.put("help-start", "<#FFB800>/emaraleague start <tournament></#FFB800> <#555555>—</#555555> <#4FC3F7>Start a tournament (admin)</#4FC3F7>");
        messages.put("help-info", "<#FFB800>/emaraleague info <tournament></#FFB800> <#555555>—</#555555> <#4FC3F7>View tournament details</#4FC3F7>");
        messages.put("help-help", "<#FFB800>/emaraleague help</#FFB800> <#555555>—</#555555> <#4FC3F7>Show this menu</#4FC3F7>");
        messages.put("player-only", "<#FF4444>This command can only be used by players.</#FF4444>");
        messages.put("reload-success", "<#00D26A>Configuration reloaded successfully.</#00D26A>");
    }

    public void loadFromFile() {
        Path messagesPath = plugin.getDataFolder().toPath().resolve("messages.yml");

        if (!Files.exists(messagesPath)) {
            saveDefaultMessages(messagesPath);
        }

        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(messagesPath)
                    .build();
            CommentedConfigurationNode root = loader.load();

            for (String key : messages.keySet()) {
                String value = root.node(key).getString();
                if (value != null) {
                    messages.put(key, value);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load messages.yml — using defaults", e);
        }
    }

    private void saveDefaultMessages(Path messagesPath) {
        try {
            Files.createDirectories(messagesPath.getParent());
            try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesPath);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save messages.yml", e);
        }
    }

    public Component get(String key, Map<String, String> placeholders) {
        String template = messages.getOrDefault(key, key);

        TagResolver[] resolvers = new TagResolver[0];
        if (placeholders != null && !placeholders.isEmpty()) {
            resolvers = placeholders.entrySet().stream()
                    .map(entry -> Placeholder.component(entry.getKey(), Component.text(entry.getValue())))
                    .toArray(TagResolver[]::new);
        }

        Component body = miniMessage.deserialize(template, resolvers);
        return MessageFormatter.prefix().append(body);
    }

    public Component get(String key) {
        return get(key, null);
    }

    public void reload() {
        loadDefaults();
        loadFromFile();
    }

    public java.util.Set<String> getKeys() {
        return messages.keySet();
    }
}
