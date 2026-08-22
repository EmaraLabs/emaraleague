package com.emaralabs.emaraleague.addons.discord;

import java.util.concurrent.atomic.AtomicBoolean;

public class DiscordBot {

    private final String token;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public DiscordBot(String token) {
        this.token = token;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public boolean sendMessage(String channel, String message) {
        return connected.get();
    }

    public void connect() {
        connected.set(true);
    }

    public void disconnect() {
        connected.set(false);
    }
}
