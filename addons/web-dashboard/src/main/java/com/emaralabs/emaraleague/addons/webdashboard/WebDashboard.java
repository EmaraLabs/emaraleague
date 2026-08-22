package com.emaralabs.emaraleague.addons.webdashboard;

import java.util.concurrent.atomic.AtomicBoolean;

public class WebDashboard {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private int port;

    public boolean start(int port) {
        this.port = port;
        running.set(true);
        return true;
    }

    public boolean stop() {
        running.set(false);
        return true;
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }
}
