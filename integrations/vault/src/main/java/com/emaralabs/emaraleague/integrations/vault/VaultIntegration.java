package com.emaralabs.emaraleague.integrations.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    private final Plugin plugin;
    private Economy economy;

    public VaultIntegration(Plugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin == null) {
            return;
        }
        
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean depositPlayer(String playerName, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return economy.depositPlayer(playerName, amount).transactionSuccess();
    }

    public boolean withdrawPlayer(String playerName, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return economy.withdrawPlayer(playerName, amount).transactionSuccess();
    }

    public double getBalance(String playerName) {
        if (!isAvailable()) {
            return 0.0;
        }
        return economy.getBalance(playerName);
    }
}
