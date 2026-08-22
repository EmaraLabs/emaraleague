package com.emaralabs.emaraleague.addons.webdashboard;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebDashboardTest {

    @Test
    void testWebDashboardCreation() {
        WebDashboard dashboard = new WebDashboard();
        assertNotNull(dashboard);
    }

    @Test
    void testStartServer() {
        WebDashboard dashboard = new WebDashboard();
        assertTrue(dashboard.start(8080));
    }

    @Test
    void testStopServer() {
        WebDashboard dashboard = new WebDashboard();
        dashboard.start(8080);
        assertTrue(dashboard.stop());
    }
}
