package com.emaralabs.emaraleague.integrations.luckperms;

import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class LuckPermsIntegration {

    private final Plugin plugin;
    private LuckPerms api;

    public LuckPermsIntegration(Plugin plugin) {
        this.plugin = plugin;
        setupLuckPerms();
    }

    private void setupLuckPerms() {
        if (plugin == null) {
            return;
        }
        
        RegisteredServiceProvider<LuckPerms> rsp = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (rsp != null) {
            api = rsp.getProvider();
        }
    }

    public boolean isAvailable() {
        return api != null;
    }

    public LuckPerms getApi() {
        return api;
    }

    public boolean hasPermission(String playerName, String permission) {
        if (!isAvailable()) {
            return false;
        }
        return api.getUserManager().getUser(playerName).getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
