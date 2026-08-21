package com.emaralabs.emaraleague.modules.duels;

public class DuelsGameMode {

    private static final String ID = "duels";
    private static final String DISPLAY_NAME = "Duels";
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 2;

    public String getId() {
        return ID;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public int getMinPlayers() {
        return MIN_PLAYERS;
    }

    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }
}
