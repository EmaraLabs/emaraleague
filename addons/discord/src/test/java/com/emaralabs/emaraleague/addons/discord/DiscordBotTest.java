package com.emaralabs.emaraleague.addons.discord;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscordBotTest {

    @Test
    void testDiscordBotCreation() {
        DiscordBot bot = new DiscordBot("token");
        assertNotNull(bot);
    }

    @Test
    void testDiscordBotConnect() {
        DiscordBot bot = new DiscordBot("token");
        assertFalse(bot.isConnected());
    }

    @Test
    void testDiscordBotSendMessage() {
        DiscordBot bot = new DiscordBot("token");
        assertFalse(bot.sendMessage("channel", "message"));
    }
}
