package com.emaralabs.emaraleague.editor.screens;

import com.emaralabs.emaraleague.core.tournament.Tournament;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import com.emaralabs.emaraleague.editor.gui.EmaraGui;
import com.emaralabs.emaraleague.editor.gui.EmaraItemBuilder;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

/**
 * Main Tournament Editor hub screen.
 * Design baseline for all other GUI screens.
 *
 * Layout:
 * Row 1: [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
 * Row 2: [ ] [G] [F] [T] [A] [R] [S] [P] [ ]
 * Row 3: [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
 * Row 4: [ ] [ ] [ ] [I] [N] [F] [O] [ ] [ ]
 * Row 5: [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ]
 * Row 6: [ ] [ ] [ ] [<] [X] [ ] [ ] [ ] [ ]
 *
 * G=General F=Format T=Teams A=Arena R=Rules S=Scoreboard P=Participants
 * I=Info N=Name F=Format O=Status <=Back X=Close
 */
public class TournamentEditorGui extends EmaraGui {

    private final TournamentManager tournamentManager;
    private final Tournament tournament;

    public TournamentEditorGui(Player player, TournamentManager tournamentManager, Tournament tournament) {
        super(player, "Tournament Editor: " + tournament.name(), 6);
        this.tournamentManager = tournamentManager;
        this.tournament = tournament;
    }

    @Override
    protected String[] getStructure() {
        return new String[]{
                "         ",
                " GFTARSP ",
                "         ",
                "   INFO  ",
                "         ",
                "    X    "
        };
    }

    @Override
    protected void build() {
        // Row 2: Sections
        gui.setItem(10, EmaraItemBuilder.section(ICON_GENERAL, "General", "Name, mode, format"));
        gui.setItem(11, EmaraItemBuilder.section(ICON_FORMAT, "Format", "Team size, structure"));
        gui.setItem(12, EmaraItemBuilder.section(ICON_TEAMS, "Teams", "Manage teams"));
        gui.setItem(13, EmaraItemBuilder.section(ICON_ARENA, "Arena", "Select arena"));
        gui.setItem(14, EmaraItemBuilder.section(ICON_RULES, "Rules", "Time, PvP, spectators"));
        gui.setItem(15, EmaraItemBuilder.section(ICON_SCOREBOARD, "Scoreboard", "Edit scoreboard"));
        gui.setItem(16, EmaraItemBuilder.section(ICON_PARTICIPANTS, "Participants", "View players"));

        // Row 4: Info display
        String status = switch (tournament.state()) {
            case REGISTRATION -> "Open for Registration";
            case STARTING -> "Starting Soon";
            case IN_PROGRESS -> "Match in Progress";
            case ENDED -> "Tournament Ended";
            case CANCELLED -> "Tournament Cancelled";
        };

        String format = tournament.isIndividual() ? "Individual" : "Team (" + tournament.teamSize() + " players)";
        String participants = tournament.isIndividual()
                ? tournament.getRegisteredCount() + " players"
                : tournament.teams().size() + " teams, " + tournament.getRegisteredCount() + " players";

        gui.setItem(30, EmaraItemBuilder.info(ICON_INFO, "Info", List.of(
                "Status: " + status,
                "Mode: " + tournament.mode(),
                "Format: " + format
        )));

        gui.setItem(31, EmaraItemBuilder.value(ICON_INFO, "Name", tournament.name(), "Tournament name"));
        gui.setItem(32, EmaraItemBuilder.value(ICON_INFO, "Format", format, "Tournament format"));
        gui.setItem(33, EmaraItemBuilder.value(ICON_INFO, "Participants", participants, "Current participants"));

        // Navigation bar
        gui.setItem(SLOT_CLOSE, EmaraItemBuilder.nav(ICON_CLOSE, "Close"));
    }
}
