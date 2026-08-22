package com.emaralabs.emaraleague.core.teleport;

import com.emaralabs.emaraleague.core.arena.Arena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportServiceTest {

    private TeleportService service;
    private Player mockPlayer;
    private World mockWorld;

    @BeforeEach
    void setUp() {
        service = new TeleportService();
        mockPlayer = mock(Player.class);
        mockWorld = mock(World.class);
    }

    @Test
    void teleportToArena_withCenter_teleportsPlayer() {
        Arena arena = new Arena("TestArena");
        Location center = new Location(mockWorld, 100, 64, 100);
        arena.setCenter(center);

        service.teleportToArena(mockPlayer, arena);
        verify(mockPlayer).teleport(center);
    }

    @Test
    void teleportToArena_noCenter_doesNothing() {
        Arena arena = new Arena("TestArena");
        service.teleportToArena(mockPlayer, arena);
        verify(mockPlayer, never()).teleport(any(Location.class));
    }

    @Test
    void teleportToLobby_withSpawn_teleportsPlayer() {
        Location lobby = new Location(mockWorld, 0, 64, 0);
        service.setLobbySpawn(lobby);

        service.teleportToLobby(mockPlayer);
        verify(mockPlayer).teleport(lobby);
    }

    @Test
    void teleportToLobby_noSpawn_doesNothing() {
        service.teleportToLobby(mockPlayer);
        verify(mockPlayer, never()).teleport(any(Location.class));
    }

    @Test
    void teleportPlayersToArena_teleportsAll() {
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        Arena arena = new Arena("TestArena");
        Location center = new Location(mockWorld, 100, 64, 100);
        arena.setCenter(center);

        service.teleportPlayersToArena(List.of(player1, player2), arena);
        verify(player1).teleport(center);
        verify(player2).teleport(center);
    }

    @Test
    void setLobbySpawn_storesLocation() {
        Location lobby = new Location(mockWorld, 0, 64, 0);
        service.setLobbySpawn(lobby);
        assertEquals(lobby, service.getLobbySpawn());
    }

    @Test
    void getLobbySpawn_default_returnsNull() {
        assertNull(service.getLobbySpawn());
    }
}
