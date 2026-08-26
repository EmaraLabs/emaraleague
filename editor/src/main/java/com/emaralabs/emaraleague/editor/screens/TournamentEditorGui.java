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
        // Row 2: Sections with click handlers
        gui.setItem(10, EmaraItemBuilder.section(ICON_GENERAL, "General", "Name, mode, format", click -> openGeneralSettings()));
        gui.setItem(11, EmaraItemBuilder.section(ICON_FORMAT, "Format", "Team size, structure", click -> openFormatSettings()));
        gui.setItem(12, EmaraItemBuilder.section(ICON_TEAMS, "Teams", "Manage teams", click -> openTeams()));
        gui.setItem(13, EmaraItemBuilder.section(ICON_ARENA, "Arena", "Select arena", click -> openArenaSelect()));
        gui.setItem(14, EmaraItemBuilder.section(ICON_RULES, "Rules", "Time, PvP, spectators", click -> openRules()));
        gui.setItem(15, EmaraItemBuilder.section(ICON_SCOREBOARD, "Scoreboard", "Edit scoreboard", click -> openScoreboard()));
        gui.setItem(16, EmaraItemBuilder.section(ICON_PARTICIPANTS, "Participants", "View players", click -> openParticipants()));

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
        gui.setItem(SLOT_CLOSE, EmaraItemBuilder.nav(ICON_CLOSE, "Close", click -> close()));
    }

    private void openGeneralSettings() {
        player.sendMessage("§7[EmaraLeague] §fGeneral settings — coming soon");
        // TODO: new GeneralSettingsGui(player, tournamentManager, tournament).open();
    }

    private void openFormatSettings() {
        player.sendMessage("§7[EmaraLeague] §fFormat settings — coming soon");
        // TODO: new FormatSettingsGui(player, tournamentManager, tournament).open();
    }

    private void openTeams() {
        player.sendMessage("§7[EmaraLeague] §fTeams management — coming soon");
        // TODO: new TeamsGui(player, tournamentManager, tournament).open();
    }

    private void openArenaSelect() {
        player.sendMessage("§7[EmaraLeague] §fArena selection — coming soon");
        // TODO: new ArenaSelectGui(player, tournamentManager, tournament).open();
    }

    private void openRules() {
        player.sendMessage("§7[EmaraLeague] §fRules configuration — coming soon");
        // TODO: new RulesGui(player, tournamentManager, tournament).open();
    }

    private void openScoreboard() {
        player.sendMessage("§7[EmaraLeague] §fScoreboard editor — coming soon");
        // TODO: new ScoreboardGui(player, tournamentManager, tournament).open();
    }

    private void openParticipants() {
        player.sendMessage("§7[EmaraLeague] §fParticipants view — coming soon");
        // TODO: new ParticipantsGui(player, tournamentManager, tournament).open();
    }
}
