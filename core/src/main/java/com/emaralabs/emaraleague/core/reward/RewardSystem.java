package com.emaralabs.emaraleague.core.reward;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Distributes configurable rewards to tournament winners.
 * Supports: console commands, Vault money, items.
 * Rewards are configured in config.yml under "rewards" section.
 */
public final class RewardSystem {

    private final Plugin plugin;
    private EconomyProvider economyProvider = EconomyProvider.NONE;
    private List<String> championCommands = new ArrayList<>();
    private List<String> runnerUpCommands = new ArrayList<>();
    private double championMoney = 0;
    private double runnerUpMoney = 0;
    private List<ItemStack> championItems = new ArrayList<>();
    private boolean enabled = false;

    public RewardSystem(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * P1-004 FIX: Inject economy provider instead of using reflection.
     */
    public void setEconomyProvider(EconomyProvider economyProvider) {
        this.economyProvider = economyProvider != null ? economyProvider : EconomyProvider.NONE;
    }

    /**
     * Load reward configuration.
     */
    public void loadConfig(org.bukkit.configuration.file.FileConfiguration config) {
        enabled = config.getBoolean("rewards.enabled", false);
        championCommands = config.getStringList("rewards.champion.commands");
        runnerUpCommands = config.getStringList("rewards.runner-up.commands");
        championMoney = config.getDouble("rewards.champion.money", 0);
        runnerUpMoney = config.getDouble("rewards.runner-up.money", 0);

        championItems = new ArrayList<>();
        List<String> itemStrings = config.getStringList("rewards.champion.items");
        for (String itemString : itemStrings) {
            try {
                Material material = Material.valueOf(itemString.toUpperCase());
                championItems.add(new ItemStack(material, 1));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid reward item: " + itemString);
            }
        }
    }

    /**
     * Give rewards to champion (winner) and runner-up.
     */
    public void distributeRewards(UUID championId, UUID runnerUpId, String tournamentName) {
        if (!enabled) {
            return;
        }

        Player champion = Bukkit.getPlayer(championId);
        Player runnerUp = Bukkit.getPlayer(runnerUpId);

        // Champion rewards
        if (champion != null && champion.isOnline()) {
            giveChampionRewards(champion, tournamentName);
        }

        // Runner-up rewards
        if (runnerUp != null && runnerUp.isOnline()) {
            giveRunnerUpRewards(runnerUp, tournamentName);
        }
    }

    /**
     * Give champion rewards to a player.
     */
    private void giveChampionRewards(Player player, String tournamentName) {
        // Execute commands
        for (String cmd : championCommands) {
            String parsed = cmd.replace("%player%", player.getName())
                              .replace("%tournament%", tournamentName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        // Give money via Vault
        if (championMoney > 0) {
            giveMoney(player, championMoney);
        }

        // Give items
        for (ItemStack item : championItems) {
            player.getInventory().addItem(item.clone());
        }

        // Feedback
        player.sendMessage(net.kyori.adventure.text.Component.text("🏆 Congratulations! You won " + tournamentName + "!", 
            com.emaralabs.emaraleague.core.ui.EmaraTheme.PRIMARY));
    }

    /**
     * Give runner-up rewards to a player.
     */
    private void giveRunnerUpRewards(Player player, String tournamentName) {
        // Execute commands
        for (String cmd : runnerUpCommands) {
            String parsed = cmd.replace("%player%", player.getName())
                              .replace("%tournament%", tournamentName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        // Give money via Vault
        if (runnerUpMoney > 0) {
            giveMoney(player, runnerUpMoney);
        }

        // Feedback
        player.sendMessage(net.kyori.adventure.text.Component.text("🥈 Good game! You placed 2nd in " + tournamentName + ".", 
            com.emaralabs.emaraleague.core.ui.EmaraTheme.INFO));
    }

    /**
     * Check if rewards are enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Give money to a player via injected economy provider.
     * P1-004 FIX: No more reflection — direct call on injected interface.
     */
    private void giveMoney(Player player, double amount) {
        if (!economyProvider.isAvailable()) {
            return;
        }
        boolean success = economyProvider.deposit(player.getName(), amount);
        if (!success) {
            plugin.getLogger().warning("Failed to deposit " + amount + " to " + player.getName());
        }
    }
}
