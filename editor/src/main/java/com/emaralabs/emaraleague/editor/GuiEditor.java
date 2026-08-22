package com.emaralabs.emaraleague.editor;

import org.bukkit.plugin.Plugin;

public class GuiEditor {

    private final Plugin plugin;

    public GuiEditor(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getTitle() {
        return "EmaraLeague Editor";
    }

    public int getSize() {
        return 27;
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
