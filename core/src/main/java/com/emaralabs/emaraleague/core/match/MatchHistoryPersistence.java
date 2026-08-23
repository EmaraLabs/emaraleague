package com.emaralabs.emaraleague.core.match;

import com.emaralabs.emaraleague.core.tournament.MatchRecord;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchHistoryPersistence {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public MatchHistoryPersistence(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "match-history.yml");
        init();
    }

    private void init() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create match-history.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save(List<MatchRecord> history) {
        config.set("matches", null);
        int i = 0;
        for (MatchRecord record : history) {
            String path = "matches." + i++;
            config.set(path + ".id", record.matchId().toString());
            config.set(path + ".tournament", record.tournamentName());
            config.set(path + ".mode", record.mode());
            config.set(path + ".teamA", record.teamAName());
            config.set(path + ".teamB", record.teamBName());
            config.set(path + ".winner", record.winnerName());
            config.set(path + ".timestamp", record.timestamp());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save match-history.yml: " + e.getMessage());
        }
    }

    public List<MatchRecord> load() {
        List<MatchRecord> history = new ArrayList<>();
        if (!config.contains("matches")) {
            return history;
        }
        var section = config.getConfigurationSection("matches");
        if (section == null) {
            return history;
        }
        for (String key : section.getKeys(false)) {
            String path = "matches." + key;
            UUID matchId = UUID.fromString(config.getString(path + ".id"));
            String tournament = config.getString(path + ".tournament");
            String mode = config.getString(path + ".mode");
            String teamA = config.getString(path + ".teamA");
            String teamB = config.getString(path + ".teamB");
            String winner = config.getString(path + ".winner");
            long timestamp = config.getLong(path + ".timestamp");
            history.add(new MatchRecord(matchId, tournament, mode, teamA, teamB, winner, timestamp));
        }
        return history;
    }
}
