package com.emaralabs.emaraleague.editor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuiEditorTest {

    @Test
    void testGuiEditorCreation() {
        GuiEditor editor = new GuiEditor(null);
        assertNotNull(editor);
    }

    @Test
    void testGuiEditorTitle() {
        GuiEditor editor = new GuiEditor(null);
        assertEquals("EmaraLeague Editor", editor.getTitle());
    }

    @Test
    void testGuiEditorSize() {
        GuiEditor editor = new GuiEditor(null);
        assertEquals(27, editor.getSize());
    }
}
