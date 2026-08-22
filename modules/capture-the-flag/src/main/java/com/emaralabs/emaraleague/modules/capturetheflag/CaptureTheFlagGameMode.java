package com.emaralabs.emaraleague.modules.capturetheflag;

import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.tournament.Match;
import com.emaralabs.emaraleague.core.tournament.Team;

public class CaptureTheFlagGameMode implements GameMode {

    private static final String ID = "ctf";
    private static final String DISPLAY_NAME = "Capture The Flag";
    private static final int MIN_PLAYERS = 4;
    private static final int MAX_PLAYERS = 16;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public int getMinPlayers() {
        return MIN_PLAYERS;
    }

    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    @Override
    public void onMatchStart(Match match) {
    }

    @Override
    public void onMatchTick(Match match) {
    }

    @Override
    public void onMatchEnd(Match match, Team winner) {
    }

    @Override
    public WinCondition getWinCondition() {
        return WinCondition.OBJECTIVE_COMPLETION;
    }
}
