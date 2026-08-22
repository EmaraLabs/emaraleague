package com.emaralabs.emaraleague.editor;

import org.bukkit.plugin.Plugin;

public class GuiEditor {

    private final Plugin plugin;
    private static final String TITLE = "EmaraLeague Editor";
    private static final int SIZE = 27;
    private static final int ROWS = 3;

    public GuiEditor(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getTitle() {
        return TITLE;
    }

    public int getSize() {
        return SIZE;
    }

    public int getRows() {
        return ROWS;
    }

    public void openEditor(Object player) {
        if (player == null) {
            return;
        }
    }

    public void createArena(String name, Object location) {
    }

    public void deleteArena(String name) {
    }

    public void listArenas() {
    }
}
