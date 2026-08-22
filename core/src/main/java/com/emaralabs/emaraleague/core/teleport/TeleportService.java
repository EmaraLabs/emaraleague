package com.emaralabs.emaraleague.core.teleport;

import com.emaralabs.emaraleague.core.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class TeleportService {

    private Location lobbySpawn;

    public void teleportToArena(Player player, Arena arena) {
        if (arena.getCenter() != null) {
            player.teleport(arena.getCenter());
        }
    }

    public void teleportToLobby(Player player) {
        if (lobbySpawn != null) {
            player.teleport(lobbySpawn);
        }
    }

    public void teleportPlayersToArena(Collection<Player> players, Arena arena) {
        for (Player player : players) {
            teleportToArena(player, arena);
        }
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }
}
