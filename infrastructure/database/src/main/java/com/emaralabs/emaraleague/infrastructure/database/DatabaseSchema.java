package com.emaralabs.emaraleague.infrastructure.database;

public class DatabaseSchema {

    public static final String TOURNAMENTS_TABLE = "tournaments";
    public static final String MATCHES_TABLE = "matches";
    public static final String TEAMS_TABLE = "teams";
    public static final String PLAYERS_TABLE = "players";

    public static String getTournamentsTable() {
        return TOURNAMENTS_TABLE;
    }

    public static String getMatchesTable() {
        return MATCHES_TABLE;
    }

    public static String getTeamsTable() {
        return TEAMS_TABLE;
    }

    public static String getPlayersTable() {
        return PLAYERS_TABLE;
    }
}
