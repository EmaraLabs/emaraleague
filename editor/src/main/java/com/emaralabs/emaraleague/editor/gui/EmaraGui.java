package com.emaralabs.emaraleague.editor.gui;

import com.emaralabs.emaraleague.core.ui.EmaraTheme;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

/**
 * Base GUI class for EmaraLeague editor screens.
 * Establishes consistent design system: colors, typography, materials, navigation.
 *
 * Design rules:
 * - No filler glass panes — empty slots are AIR
 * - Max 8 materials per screen
 * - Consistent navigation bar (bottom row)
 * - State shown as text, not just color
 */
public abstract class EmaraGui {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    // Materials (max 8 per screen)
    protected static final Material ICON_GENERAL = Material.BOOK;
    protected static final Material ICON_FORMAT = Material.COMPARATOR;
    protected static final Material ICON_TEAMS = Material.PLAYER_HEAD;
    protected static final Material ICON_ARENA = Material.GRASS_BLOCK;
    protected static final Material ICON_RULES = Material.REDSTONE;
    protected static final Material ICON_SCOREBOARD = Material.OAK_SIGN;
    protected static final Material ICON_PARTICIPANTS = Material.PAPER;
    protected static final Material ICON_INFO = Material.MAP;
    protected static final Material ICON_BACK = Material.ARROW;
    protected static final Material ICON_CLOSE = Material.BARRIER;
    protected static final Material ICON_SAVE = Material.EMERALD;

    // Navigation slots (bottom row, 0-indexed)
    public static final int SLOT_BACK = 45;
    public static final int SLOT_CLOSE = 49;
    public static final int SLOT_SAVE = 53;

    protected final Player player;
    protected final String title;
    protected final int rows;
    protected Gui gui;
    protected Window window;

    protected EmaraGui(Player player, String title, int rows) {
        this.player = player;
        this.title = title;
        this.rows = rows;
    }

    /**
     * Build the GUI content. Called once on open.
     */
    protected abstract void build();

    /**
     * Get the structure string for InvUI.
     * Override to define layout.
     */
    protected abstract String[] getStructure();

    /**
     * Open the GUI for the player.
     */
    public void open() {
        gui = Gui.normal()
                .setStructure(getStructure())
                .build();

        build();

        window = Window.single()
                .setViewer(player)
                .setTitle(buildTitleString())
                .setGui(gui)
                .build();

        window.open();
    }

    /**
     * Build the window title as MiniMessage string.
     */
    protected String buildTitleString() {
        return "<gold>✦ </gold><bold>" + title + "</bold><gold> ✦</gold>";
    }

    /**
     * Close the GUI.
     */
    public void close() {
        if (window != null) {
            window.close();
        }
    }

    // ── Item Builders ─────────────────────────────────────────────

    /**
     * Create a section button (main navigation item).
     */
    protected Item sectionButton(Material material, String name, String description) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        builder.addLoreLines(description);
        return new SimpleItem(builder);
    }

    /**
     * Create a navigation button (back/close/save).
     */
    protected Item navButton(Material material, String name, String color) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        return new SimpleItem(builder);
    }

    /**
     * Create an info display item (view-only).
     */
    protected Item infoItem(Material material, String name, List<String> lore) {
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
    protected Item toggleButton(String name, String description, boolean enabled) {
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
    protected Item valueItem(Material material, String name, String value, String description) {
        ItemBuilder builder = new ItemBuilder(material);
        builder.setDisplayName(name);
        builder.addLoreLines(description);
        builder.addLoreLines("Current: " + value);
        return new SimpleItem(builder);
    }

    // ── Navigation Bar ────────────────────────────────────────────

    /**
     * Add standard navigation bar (back, close).
     */
    protected void addNavigationBar(boolean showBack, boolean showSave) {
        if (showBack) {
            gui.setItem(SLOT_BACK, navButton(ICON_BACK, "Back", "gray"));
        }
        gui.setItem(SLOT_CLOSE, navButton(ICON_CLOSE, "Close", "red"));
        if (showSave) {
            gui.setItem(SLOT_SAVE, navButton(ICON_SAVE, "Save", "green"));
        }
    }

    // ── Utility ───────────────────────────────────────────────────

    /**
     * Create description lore line.
     */
    protected String desc(String text) {
        return "<gray>" + text + "</gray>";
    }

    /**
     * Create value lore line.
     */
    protected String value(String label, String value) {
        return "<gray>" + label + ": </gray><blue>" + value + "</blue>";
    }
}
