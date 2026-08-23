package com.emaralabs.emaraleague.core.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArenaPersistence {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    public ArenaPersistence(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create arenas.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save(ArenaManager arenaManager) {
        config.set("arenas", null);
        for (Arena arena : arenaManager.getArenas()) {
            String path = "arenas." + arena.getId().toString();
            config.set(path + ".name", arena.getName());
            config.set(path + ".state", arena.getState().name());
            if (arena.getCenter() != null) {
                config.set(path + ".center.world", arena.getCenter().getWorld().getName());
                config.set(path + ".center.x", arena.getCenter().getX());
                config.set(path + ".center.y", arena.getCenter().getY());
                config.set(path + ".center.z", arena.getCenter().getZ());
                config.set(path + ".center.yaw", arena.getCenter().getYaw());
                config.set(path + ".center.pitch", arena.getCenter().getPitch());
            }
            if (arena.getLobbySpawn() != null) {
                config.set(path + ".lobby.world", arena.getLobbySpawn().getWorld().getName());
                config.set(path + ".lobby.x", arena.getLobbySpawn().getX());
                config.set(path + ".lobby.y", arena.getLobbySpawn().getY());
                config.set(path + ".lobby.z", arena.getLobbySpawn().getZ());
                config.set(path + ".lobby.yaw", arena.getLobbySpawn().getYaw());
                config.set(path + ".lobby.pitch", arena.getLobbySpawn().getPitch());
            }
            if (arena.getSpawnA() != null) {
                config.set(path + ".spawnA.world", arena.getSpawnA().getWorld().getName());
                config.set(path + ".spawnA.x", arena.getSpawnA().getX());
                config.set(path + ".spawnA.y", arena.getSpawnA().getY());
                config.set(path + ".spawnA.z", arena.getSpawnA().getZ());
                config.set(path + ".spawnA.yaw", arena.getSpawnA().getYaw());
                config.set(path + ".spawnA.pitch", arena.getSpawnA().getPitch());
            }
            if (arena.getSpawnB() != null) {
                config.set(path + ".spawnB.world", arena.getSpawnB().getWorld().getName());
                config.set(path + ".spawnB.x", arena.getSpawnB().getX());
                config.set(path + ".spawnB.y", arena.getSpawnB().getY());
                config.set(path + ".spawnB.z", arena.getSpawnB().getZ());
                config.set(path + ".spawnB.yaw", arena.getSpawnB().getYaw());
                config.set(path + ".spawnB.pitch", arena.getSpawnB().getPitch());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arenas.yml: " + e.getMessage());
        }
    }

    public void load(ArenaManager arenaManager) {
        if (!config.contains("arenas")) {
            return;
        }
        var section = config.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String path = "arenas." + key;
            String name = config.getString(path + ".name");
            if (name == null) {
                continue;
            }
            Arena arena = arenaManager.createArena(name);
            // Always reset to LOBBY on load — previous state may be invalid after restart
            // (e.g. arena was INGAME when server crashed, LOBBY->INGAME is invalid transition)
            String stateName = config.getString(path + ".state", "LOBBY");
            try {
                ArenaState state = ArenaState.valueOf(stateName);
                // Only set if valid transition from LOBBY
                if (state == ArenaState.LOBBY && arena.getState() != ArenaState.LOBBY) {
                    arena.setState(state);
                }
                // Non-LOBBY states are ignored — arena starts fresh at LOBBY
            } catch (IllegalArgumentException e) {
                // Invalid state name, keep default LOBBY
            }
            if (config.contains(path + ".center")) {
                String worldName = config.getString(path + ".center.world");
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(path + ".center.x");
                    double y = config.getDouble(path + ".center.y");
                    double z = config.getDouble(path + ".center.z");
                    float yaw = (float) config.getDouble(path + ".center.yaw");
                    float pitch = (float) config.getDouble(path + ".center.pitch");
                    arena.setCenter(new Location(world, x, y, z, yaw, pitch));
                }
            }
            if (config.contains(path + ".lobby")) {
                String worldName = config.getString(path + ".lobby.world");
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(path + ".lobby.x");
                    double y = config.getDouble(path + ".lobby.y");
                    double z = config.getDouble(path + ".lobby.z");
                    float yaw = (float) config.getDouble(path + ".lobby.yaw");
                    float pitch = (float) config.getDouble(path + ".lobby.pitch");
                    arena.setLobbySpawn(new Location(world, x, y, z, yaw, pitch));
                }
            }
            if (config.contains(path + ".spawnA")) {
                String worldName = config.getString(path + ".spawnA.world");
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(path + ".spawnA.x");
                    double y = config.getDouble(path + ".spawnA.y");
                    double z = config.getDouble(path + ".spawnA.z");
                    float yaw = (float) config.getDouble(path + ".spawnA.yaw");
                    float pitch = (float) config.getDouble(path + ".spawnA.pitch");
                    arena.setSpawnA(new Location(world, x, y, z, yaw, pitch));
                }
            }
            if (config.contains(path + ".spawnB")) {
                String worldName = config.getString(path + ".spawnB.world");
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(path + ".spawnB.x");
                    double y = config.getDouble(path + ".spawnB.y");
                    double z = config.getDouble(path + ".spawnB.z");
                    float yaw = (float) config.getDouble(path + ".spawnB.yaw");
                    float pitch = (float) config.getDouble(path + ".spawnB.pitch");
                    arena.setSpawnB(new Location(world, x, y, z, yaw, pitch));
                }
            }
        }
    }
}
