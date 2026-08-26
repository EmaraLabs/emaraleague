package com.emaralabs.emaraleague.core.arena;

import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Arena with reusable spawn slots (A-D).
 * Spawn slots are independent of participant identity — the tournament
 * assigns players or teams to slots at match time.
 */
public class Arena {

    private final UUID id;
    private final String name;
    private ArenaState state;
    private Location center;
    private Location lobbySpawn;
    private final Map<SpawnSlot, Location> spawnSlots = new EnumMap<>(SpawnSlot.class);

    public Arena(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.state = ArenaState.LOBBY;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState newState) {
        validateStateTransition(this.state, newState);
        this.state = newState;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    // ── Spawn Slot API ────────────────────────────────────────────

    public void setSpawn(SpawnSlot slot, Location location) {
        spawnSlots.put(slot, location);
    }

    public Location getSpawn(SpawnSlot slot) {
        return spawnSlots.getOrDefault(slot, center);
    }

    public boolean hasSpawn(SpawnSlot slot) {
        return spawnSlots.containsKey(slot);
    }

    public Map<SpawnSlot, Location> getSpawnSlots() {
        return Map.copyOf(spawnSlots);
    }

    // ── Legacy compatibility ──────────────────────────────────────

    @Deprecated
    public Location getSpawnA() {
        return getSpawn(SpawnSlot.A);
    }

    @Deprecated
    public void setSpawnA(Location spawnA) {
        setSpawn(SpawnSlot.A, spawnA);
    }

    @Deprecated
    public Location getSpawnB() {
        return getSpawn(SpawnSlot.B);
    }

    @Deprecated
    public void setSpawnB(Location spawnB) {
        setSpawn(SpawnSlot.B, spawnB);
    }

    @Deprecated
    public boolean hasSpawnPoints() {
        return hasSpawn(SpawnSlot.A) && hasSpawn(SpawnSlot.B);
    }

    // ── State transitions ─────────────────────────────────────────

    public boolean canTransitionTo(ArenaState next) {
        return switch (state) {
            case LOBBY -> next == ArenaState.STARTING;
            case STARTING -> next == ArenaState.INGAME;
            case INGAME -> next == ArenaState.ENDING;
            case ENDING -> next == ArenaState.RESETTING;
            case RESETTING -> next == ArenaState.LOBBY;
        };
    }

    private void validateStateTransition(ArenaState current, ArenaState next) {
        if (!canTransitionTo(next)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition: %s -> %s", current, next)
            );
        }
    }
}
