package com.emaralabs.emaraleague.api;

/**
 * SPI interface for EmaraLeague addons.
 * Addons implement this interface to hook into the core plugin.
 */
public interface EmaraAddon {

    /**
     * Unique identifier for this addon.
     */
    String getId();

    /**
     * Display name of this addon.
     */
    String getName();

    /**
     * Version string of this addon.
     */
    String getVersion();

    /**
     * Minimum API version required by this addon.
     * If core API version is lower, addon will not be enabled.
     */
    int getRequiredApiVersion();

    /**
     * Called when the addon is enabled.
     * @param api the EmaraLeague API instance
     */
    void onEnable(EmaraLeagueAPI api);

    /**
     * Called when the addon is disabled.
     */
    void onDisable();
}
