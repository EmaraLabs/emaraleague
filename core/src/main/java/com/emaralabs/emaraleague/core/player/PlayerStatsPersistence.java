package com.emaralabs.emaraleague.core.player;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerStatsPersistence {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public PlayerStatsPersistence(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-stats.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create player-stats.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save(PlayerStats stats) {
        config.set("players", null);
        // Note: PlayerStats doesn't expose all player IDs, so we save what we can
        // This is a limitation of the current PlayerStats design
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save player-stats.yml: " + e.getMessage());
        }
    }

    public void savePlayer(UUID playerId, int wins, int losses, int kills, int deaths) {
        String path = "players." + playerId.toString();
        config.set(path + ".wins", wins);
        config.set(path + ".losses", losses);
        config.set(path + ".kills", kills);
        config.set(path + ".deaths", deaths);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save player-stats.yml: " + e.getMessage());
        }
    }

    public void load(PlayerStats stats) {
        if (!config.contains("players")) {
            return;
        }
        var section = config.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            int wins = config.getInt("players." + key + ".wins", 0);
            int losses = config.getInt("players." + key + ".losses", 0);
            int kills = config.getInt("players." + key + ".kills", 0);
            int deaths = config.getInt("players." + key + ".deaths", 0);
            for (int i = 0; i < wins; i++) stats.addWin(playerId);
            for (int i = 0; i < losses; i++) stats.addLoss(playerId);
            for (int i = 0; i < kills; i++) stats.addKill(playerId);
            for (int i = 0; i < deaths; i++) stats.addDeath(playerId);
        }
    }
}
