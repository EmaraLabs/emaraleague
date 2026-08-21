package com.emaralabs.emaraleague.integrations.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EmaraLeagueExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "emaraleague";
    }

    @Override
    public @NotNull String getAuthor() {
        return "EmaraLabs";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("wins")) {
            return "0";
        }
        if (params.equalsIgnoreCase("losses")) {
            return "0";
        }
        if (params.equalsIgnoreCase("rank")) {
            return "Unranked";
        }
        return null;
    }
}
