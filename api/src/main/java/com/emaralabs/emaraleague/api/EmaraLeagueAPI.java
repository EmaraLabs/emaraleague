package com.emaralabs.emaraleague.api;

import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

/**
 * Public API for EmaraLeague core plugin.
 * Addons use this interface to interact with the core.
 */
public interface EmaraLeagueAPI {

    /**
     * Returns the current API version.
     * Addons should check this against their required version.
     */
    int getApiVersion();

    /**
     * Returns the tournament manager for CRUD operations.
     */
    Object getTournamentManager();

    /**
     * Returns the arena manager for arena operations.
     */
    Object getArenaManager();

    /**
     * Returns the match engine for match lifecycle.
     */
    Object getMatchEngine();

    /**
     * Returns the game mode registry.
     */
    Object getGameModeRegistry();

    /**
     * Returns the player session manager.
     */
    Object getPlayerSessionManager();

    /**
     * Registers an addon with the core plugin.
     * @param addon the addon to register
     * @throws IllegalArgumentException if addon requires higher API version
     */
    void registerAddon(EmaraAddon addon);

    /**
     * Unregisters an addon by ID.
     * @param addonId the addon ID to unregister
     */
    void unregisterAddon(String addonId);

    /**
     * Returns all registered addons.
     */
    List<EmaraAddon> getAddons();

    /**
     * Checks if an addon is enabled.
     */
    boolean isAddonEnabled(String addonId);

    /**
     * Registers a custom game mode.
     */
    void registerGameMode(Object gameMode);

    /**
     * Unregisters a game mode by ID.
     */
    void unregisterGameMode(String gameModeId);

    /**
     * Broadcasts a message to all players in a tournament.
     */
    void broadcastToTournament(String tournamentName, Component message);

    /**
     * Broadcasts a message to all players in a match.
     */
    void broadcastToMatch(UUID matchId, Component message);
}
