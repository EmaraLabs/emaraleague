package com.emaralabs.emaraleague.editor.gui;

import org.bukkit.Material;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.List;

/**
 * Thin abstraction over InvUI 1.49 ItemBuilder.
 * Centralizes item creation to prevent API details from scattering across GUI classes.
 *
 * Actual InvUI 1.49 API:
 * - setDisplayName(String)
 * - addLoreLines(String...)
 * - addLoreLines(List<ComponentWrapper>)
 */
public final class EmaraItemBuilder {

    private EmaraItemBuilder() {}

    /**
     * Create a section button (main navigation item).
     */
    public static Item section(Material material, String name, String description) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        builder.addLoreLines(description);
        return new SimpleItem(builder);
    }

    /**
     * Create a navigation button (back/close/save).
     */
    public static Item nav(Material material, String name) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        return new SimpleItem(builder);
    }

    /**
     * Create an info display item (view-only).
     */
    public static Item info(Material material, String name, List<String> lore) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        for (String line : lore) {
            builder.addLoreLines(line);
        }
        return new SimpleItem(builder);
    }

    /**
     * Create a toggle button (ON/OFF state).
     */
    public static Item toggle(String name, String description, boolean enabled) {
        ItemBuilder builder = new ItemBuilder(enabled ? Material.LEVER : Material.REDSTONE_TORCH);
        builder.setDisplayName(name);
        builder.addLoreLines(description);
        builder.addLoreLines("Status: " + (enabled ? "ON" : "OFF"));
        builder.addLoreLines("Click to toggle");
        return new SimpleItem(builder);
    }

    /**
     * Create a value display item with current value.
     */
    public static Item value(Material material, String name, String value, String description) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        builder.addLoreLines(description);
        builder.addLoreLines("Current: " + value);
        return new SimpleItem(builder);
    }
}
