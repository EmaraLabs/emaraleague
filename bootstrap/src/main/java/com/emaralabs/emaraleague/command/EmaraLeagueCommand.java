package com.emaralabs.emaraleague.command;

import com.emaralabs.emaraleague.core.arena.Arena;
import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.tournament.BracketType;
import com.emaralabs.emaraleague.core.tournament.Team;
import com.emaralabs.emaraleague.core.tournament.Tournament;
import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import com.emaralabs.emaraleague.core.tournament.TournamentState;
import com.emaralabs.emaraleague.core.ui.EmaraTheme;
import com.emaralabs.emaraleague.core.ui.InputValidator;
import com.emaralabs.emaraleague.core.ui.MessageFormatter;
import com.emaralabs.emaraleague.core.ui.MessageRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EmaraLeagueCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final MessageRegistry messages;
    private final TournamentManager tournamentManager;
    private final ArenaManager arenaManager;

    private static final List<String> GAME_MODES = List.of(
            "duels", "spleef", "sumo", "tnt-run", "parkour", "capture-the-flag"
    );

    private static final List<SubCommand> SUB_COMMANDS = List.of(
            new SubCommand("create", "/emaraleague create <name> <mode>", "Create a new tournament", "emaraleague.create"),
            new SubCommand("delete", "/emaraleague delete <name>", "Delete a tournament", "emaraleague.admin"),
            new SubCommand("cancel", "/emaraleague cancel <name>", "Cancel an ongoing tournament", "emaraleague.admin"),
            new SubCommand("join", "/emaraleague join <tournament>", "Join a tournament", "emaraleague.play"),
            new SubCommand("leave", "/emaraleague leave", "Leave your current tournament", "emaraleague.play"),
            new SubCommand("team", "/emaraleague team <join|leave|list>", "Manage teams", "emaraleague.play"),
            new SubCommand("start", "/emaraleague start <tournament>", "Start a tournament", "emaraleague.admin"),
            new SubCommand("info", "/emaraleague info <tournament>", "View tournament info", "emaraleague.use"),
            new SubCommand("arena", "/emaraleague arena <create|setcenter|setlobby|list|delete>", "Manage arenas", "emaraleague.admin"),
            new SubCommand("history", "/emaraleague history", "View match history", "emaraleague.use"),
            new SubCommand("stats", "/emaraleague stats [player]", "View player statistics", "emaraleague.use"),
            new SubCommand("spectate", "/emaraleague spectate <tournament>", "Spectate an active match", "emaraleague.use"),
            new SubCommand("rejoin", "/emaraleague rejoin", "Rejoin your active match", "emaraleague.play"),
            new SubCommand("help", "/emaraleague help [command]", "Show help", "emaraleague.use"),
            new SubCommand("reload", "/emaraleague reload", "Reload configuration", "emaraleague.reload")
    );

    public EmaraLeagueCommand(Plugin plugin, TournamentManager tournamentManager, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.tournamentManager = tournamentManager;
        this.arenaManager = arenaManager;
        this.messages = new MessageRegistry(plugin);
    }

    public String getName() {
        return "emaraleague";
    }

    public List<String> getAliases() {
        return List.of("el", "league");
    }

    public String getDescription() {
        return "EmaraLeague tournament management";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (!hasPermission(sender, sub)) {
            sender.sendMessage(messages.get("no-permission", Map.of("permission", "emaraleague." + sub)));
            return true;
        }

        try {
            switch (sub) {
                case "create" -> handleCreate(sender, args);
                case "delete" -> handleDelete(sender, args);
                case "cancel" -> handleCancel(sender, args);
                case "join" -> handleJoin(sender, args);
                case "leave" -> handleLeave(sender);
                case "team" -> handleTeam(sender, args);
                case "start" -> handleStart(sender, args);
                case "info" -> handleInfo(sender, args);
                case "arena" -> handleArena(sender, args);
                case "history" -> handleHistory(sender);
                case "stats" -> handleStats(sender, args);
                case "spectate" -> handleSpectate(sender, args);
                case "rejoin" -> handleRejoin(sender);
                case "help" -> sendHelp(sender);
                case "reload" -> handleReload(sender);
                default -> sender.sendMessage(messages.get("unknown-command"));
            }
        } catch (IllegalStateException e) {
            // Business-rule violations → player-facing message, no console stack trace
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        } catch (Exception e) {
            // Unexpected bugs → generic player message + full console log
            sender.sendMessage(MessageFormatter.error("An unexpected error occurred. Please report this to an admin."));
            plugin.getLogger().severe("Unexpected error executing /emaraleague " + sub + ": " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague create <name> <mode>")));
            return;
        }

        String name = args[1];
        String mode = args[2].toLowerCase();

        var nameResult = InputValidator.validateTournamentName(name);
        if (!nameResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(nameResult.getErrorMessage()));
            return;
        }

        var modeResult = InputValidator.validateGameMode(mode);
        if (!modeResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(modeResult.getErrorMessage()));
            return;
        }

        if (!GAME_MODES.contains(mode)) {
            sender.sendMessage(messages.get("invalid-game-mode", Map.of(
                    "mode", mode,
                    "modes", String.join(", ", GAME_MODES))));
            return;
        }

        try {
            tournamentManager.createTournament(name, mode, BracketType.SINGLE_ELIMINATION);
            sender.sendMessage(messages.get("tournament-created", Map.of("name", name)));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        }
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague join <tournament>")));
            return;
        }

        String name = args[1];
        var nameResult = InputValidator.validateTournamentName(name);
        if (!nameResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(nameResult.getErrorMessage()));
            return;
        }

        if (!tournamentManager.exists(name)) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        // Bug 1 fix: Check if already registered (even without team)
        if (tournamentManager.isPlayerRegistered(name, player.getUniqueId())) {
            sender.sendMessage(MessageFormatter.error("You are already registered in this tournament."));
            return;
        }

        // Bug 2 fix: Auto-create teams if none exist, then auto-assign
        Tournament tournament = tournamentManager.getTournament(name).get();
        if (tournament.teams().isEmpty()) {
            // Auto-create 2 teams for 1v1 modes, or 4 teams for FFA modes
            int teamCount = tournament.mode().equals("duels") ? 2 : 4;
            for (int i = 1; i <= teamCount; i++) {
                tournamentManager.addTeam(name, new Team("Team" + i, i));
            }
        }

        // Register player
        tournamentManager.registerPlayer(name, player.getUniqueId());

        // Auto-assign to team with fewest players
        tournamentManager.autoAssignToTeam(name, player.getUniqueId());

        Optional<Team> assignedTeam = tournamentManager.getTeamForPlayer(name, player.getUniqueId());
        String teamName = assignedTeam.map(Team::name).orElse("Unknown");

        sender.sendMessage(messages.get("tournament-joined", Map.of("name", name)));
        sender.sendMessage(MessageFormatter.info("You have been assigned to " + teamName));
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        // Find player's tournament and remove from team + unregister
        for (Tournament tournament : tournamentManager.getTournaments()) {
            if (tournament.isPlayerRegistered(player.getUniqueId())) {
                // Guard: cannot leave once tournament has started
                if (tournament.state() != TournamentState.REGISTRATION) {
                    sender.sendMessage(MessageFormatter.error("You cannot leave — the tournament has already started."));
                    return;
                }
                // Remove from team
                Optional<Team> team = tournamentManager.getTeamForPlayer(tournament.name(), player.getUniqueId());
                if (team.isPresent()) {
                    tournamentManager.removePlayerFromTeam(tournament.name(), team.get().id(), player.getUniqueId());
                }
                // Unregister
                tournamentManager.unregisterPlayer(tournament.name(), player.getUniqueId());
                sender.sendMessage(messages.get("tournament-left"));
                return;
            }
        }

        sender.sendMessage(MessageFormatter.error("You are not in any tournament."));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague delete <tournament>")));
            return;
        }

        String name = args[1];
        if (!tournamentManager.exists(name)) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
            return;
        }

        tournamentManager.deleteTournament(name);
        sender.sendMessage(MessageFormatter.success("Tournament '" + name + "' deleted."));
    }

    private void handleCancel(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague cancel <tournament>")));
            return;
        }

        String name = args[1];
        if (!tournamentManager.exists(name)) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
            return;
        }

        Tournament tournament = tournamentManager.getTournament(name).get();
        if (tournament.state() == TournamentState.REGISTRATION) {
            sender.sendMessage(MessageFormatter.error("Tournament has not started yet. Use /el delete instead."));
            return;
        }
        if (tournament.state() == TournamentState.ENDED || tournament.state() == TournamentState.CANCELLED) {
            sender.sendMessage(MessageFormatter.error("Tournament is already finished."));
            return;
        }

        // Cancel tournament
        tournamentManager.cancelTournament(name);

        // Cleanup: end active matches, teleport players back
        com.emaralabs.emaraleague.core.match.MatchEngine matchEngine =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchEngine();
        for (com.emaralabs.emaraleague.core.tournament.Match match : matchEngine.getMatches(name)) {
            if (match.state() != com.emaralabs.emaraleague.core.tournament.MatchState.ENDED) {
                try {
                    matchEngine.endMatch(match.id(), null);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to end match " + match.id() + " during cancel: " + e.getMessage());
                }
            }
        }

        sender.sendMessage(MessageFormatter.success("Tournament '" + name + "' has been cancelled."));
    }

    private void handleHistory(CommandSender sender) {
        java.util.List<com.emaralabs.emaraleague.core.tournament.MatchRecord> history =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchHistoryPersistence().load();

        if (history.isEmpty()) {
            sender.sendMessage(MessageFormatter.info("No match history yet."));
            return;
        }

        sender.sendMessage(MessageFormatter.header("Recent Matches"));
        int count = Math.min(5, history.size());
        for (int i = history.size() - 1; i >= history.size() - count && i >= 0; i--) {
            com.emaralabs.emaraleague.core.tournament.MatchRecord record = history.get(i);
            String line = String.format("%s vs %s — %s won (%s)",
                    record.teamAName(), record.teamBName(), record.winnerName(), record.mode());
            sender.sendMessage(Component.text("  " + line, EmaraTheme.INFO));
        }
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        com.emaralabs.emaraleague.core.player.PlayerStats stats =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchEngine().getPlayerStats();
        if (stats == null) {
            sender.sendMessage(MessageFormatter.error("Statistics not available."));
            return;
        }

        UUID playerId = player.getUniqueId();
        int wins = stats.getWins(playerId);
        int losses = stats.getLosses(playerId);
        int kills = stats.getKills(playerId);
        int deaths = stats.getDeaths(playerId);
        double winRate = stats.getWinRate(playerId);
        double kd = stats.getKDRatio(playerId);

        sender.sendMessage(MessageFormatter.header("Statistics for " + player.getName()));
        sender.sendMessage(Component.text("  Wins: ", EmaraTheme.MUTED)
                .append(Component.text(wins, EmaraTheme.SUCCESS)));
        sender.sendMessage(Component.text("  Losses: ", EmaraTheme.MUTED)
                .append(Component.text(losses, EmaraTheme.ERROR)));
        sender.sendMessage(Component.text("  Kills: ", EmaraTheme.MUTED)
                .append(Component.text(kills, EmaraTheme.PRIMARY)));
        sender.sendMessage(Component.text("  Deaths: ", EmaraTheme.MUTED)
                .append(Component.text(deaths, EmaraTheme.WARNING)));
        sender.sendMessage(Component.text("  Win Rate: ", EmaraTheme.MUTED)
                .append(Component.text(String.format("%.1f%%", winRate * 100), EmaraTheme.INFO)));
        sender.sendMessage(Component.text("  K/D Ratio: ", EmaraTheme.MUTED)
                .append(Component.text(String.format("%.2f", kd), EmaraTheme.ACCENT)));
    }

    private void handleTeam(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team <join|leave|list>")));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "join" -> handleTeamJoin(sender, args);
            case "leave" -> handleTeamLeave(sender);
            case "list" -> handleTeamList(sender, args);
            default -> sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team <join|leave|list>")));
        }
    }

    private void handleTeamJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team join <tournament> <team>")));
            return;
        }

        String tournamentName = args[2];
        String teamName = args[3];

        Optional<Tournament> tournament = tournamentManager.getTournament(tournamentName);
        if (tournament.isEmpty()) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", tournamentName)));
            return;
        }

        Optional<Team> team = tournament.get().teams().stream()
                .filter(t -> t.name().equalsIgnoreCase(teamName))
                .findFirst();

        if (team.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("Team not found: " + teamName));
            return;
        }

        // Bug 4 fix: Check if player is already in ANY team in this tournament
        if (tournamentManager.getTeamForPlayer(tournamentName, player.getUniqueId()).isPresent()) {
            sender.sendMessage(MessageFormatter.error("You are already in a team in this tournament."));
            return;
        }

        try {
            tournamentManager.assignPlayerToTeam(tournamentName, team.get().id(), player.getUniqueId());
            sender.sendMessage(MessageFormatter.success("You joined team " + team.get().name()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        }
    }

    private void handleTeamLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        // Find player's team across all tournaments
        for (Tournament tournament : tournamentManager.getTournaments()) {
            Optional<Team> team = tournamentManager.getTeamForPlayer(tournament.name(), player.getUniqueId());
            if (team.isPresent()) {
                tournamentManager.removePlayerFromTeam(tournament.name(), team.get().id(), player.getUniqueId());
                sender.sendMessage(MessageFormatter.success("You left team " + team.get().name()));
                return;
            }
        }

        sender.sendMessage(MessageFormatter.error("You are not in any team."));
    }

    private void handleTeamList(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague team list <tournament>")));
            return;
        }

        String tournamentName = args[2];
        Optional<Tournament> tournament = tournamentManager.getTournament(tournamentName);
        if (tournament.isEmpty()) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", tournamentName)));
            return;
        }

        sender.sendMessage(MessageFormatter.header("Teams in " + tournamentName));
        for (Team team : tournament.get().teams()) {
            Component line = Component.text()
                    .append(Component.text("  " + team.name(), EmaraTheme.WARNING))
                    .append(Component.text(" (" + team.getPlayerCount() + " players)", EmaraTheme.MUTED))
                    .build();
            sender.sendMessage(line);
        }
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague start <tournament>")));
            return;
        }

        String name = args[1];
        var nameResult = InputValidator.validateTournamentName(name);
        if (!nameResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(nameResult.getErrorMessage()));
            return;
        }

        // Check for solo mode (testing)
        boolean soloMode = args.length >= 3 && args[2].equalsIgnoreCase("solo");
        if (soloMode) {
            Tournament tournament = tournamentManager.getTournament(name).orElse(null);
            if (tournament == null) {
                sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
                return;
            }
            if (tournament.teams().size() < 2) {
                Team dummyTeam = new Team("Dummy", 2);
                tournamentManager.addTeam(name, dummyTeam);
            }
            sender.sendMessage(MessageFormatter.info("Starting tournament in SOLO mode (testing only)"));
            sender.sendMessage(MessageFormatter.muted("Dummy team created — no real players assigned."));
        } else {
            // Normal validation
            if (!tournamentManager.canStart(name)) {
                sender.sendMessage(MessageFormatter.error("Tournament needs at least 2 teams with 1 player each to start."));
                return;
            }
        }

        try {
            // Transition tournament state
            tournamentManager.transitionState(name, TournamentState.STARTING);
            tournamentManager.transitionState(name, TournamentState.IN_PROGRESS);

            // Generate bracket and start first match
            com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket bracketGen =
                    new com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket();
            com.emaralabs.emaraleague.core.match.MatchEngine matchEngine =
                    com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchEngine();

            com.emaralabs.emaraleague.core.bracket.Bracket bracket =
                    matchEngine.generateBracket(name, bracketGen);

            // Start first match
            var firstMatch = matchEngine.getNextMatch(name);
            if (firstMatch.isPresent()) {
                matchEngine.startMatch(firstMatch.get().id());
                sender.sendMessage(messages.get("tournament-started", Map.of("name", name)));
                sender.sendMessage(MessageFormatter.info("First match starting..."));
            } else {
                sender.sendMessage(MessageFormatter.error("No matches available to start."));
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        } catch (IllegalStateException e) {
            sender.sendMessage(MessageFormatter.error("Tournament cannot be started in its current state."));
        }
    }

    private void handleArena(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena <create|setcenter|setlobby|list|delete>")));
            return;
        }

        switch (args[1].toLowerCase()) {
        case "create" -> handleArenaCreate(sender, args);
        case "setcenter" -> handleArenaSetCenter(sender, args);
        case "setlobby" -> handleArenaSetLobby(sender, args);
        case "setspawn" -> handleArenaSetSpawn(sender, args);
        case "list" -> handleArenaList(sender);
        case "delete" -> handleArenaDelete(sender, args);
        default -> sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena <create|setcenter|setlobby|setspawn|list|delete>")));
        }
    }

    private void handleArenaCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena create <name>")));
            return;
        }

        String name = args[2];
        var nameResult = InputValidator.validateArenaName(name);
        if (!nameResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(nameResult.getErrorMessage()));
            return;
        }

        try {
            arenaManager.createArena(name);
            sender.sendMessage(MessageFormatter.success("Arena '" + name + "' created."));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
        }
    }

    private void handleArenaSetCenter(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena setcenter <name>")));
            return;
        }

        String name = args[2];
        Optional<Arena> arena = arenaManager.getArena(name);
        if (arena.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("Arena not found: " + name));
            return;
        }

        arena.get().setCenter(player.getLocation());
        sender.sendMessage(MessageFormatter.success("Arena '" + name + "' center set to your location."));
    }

    private void handleArenaSetLobby(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena setlobby <name>")));
            return;
        }

        String name = args[2];
        var arenaOpt = arenaManager.getArena(name);
        if (arenaOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("Arena not found: " + name));
            return;
        }

        arenaOpt.get().setLobbySpawn(player.getLocation());
        sender.sendMessage(MessageFormatter.success("Lobby spawn set for arena '" + name + "'."));
    }

    private void handleArenaSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena setspawn <name> <a|b>")));
            return;
        }

        String name = args[2];
        String team = args[3].toLowerCase();
        var arenaOpt = arenaManager.getArena(name);
        if (arenaOpt.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("Arena not found: " + name));
            return;
        }

        if (team.equals("a")) {
            arenaOpt.get().setSpawnA(player.getLocation());
            sender.sendMessage(MessageFormatter.success("Team A spawn set for arena '" + name + "'."));
        } else if (team.equals("b")) {
            arenaOpt.get().setSpawnB(player.getLocation());
            sender.sendMessage(MessageFormatter.success("Team B spawn set for arena '" + name + "'."));
        } else {
            sender.sendMessage(MessageFormatter.error("Invalid team. Use 'a' or 'b'."));
        }
    }

    private void handleArenaList(CommandSender sender) {
        List<Arena> arenas = arenaManager.getArenas();
        if (arenas.isEmpty()) {
            sender.sendMessage(MessageFormatter.info("No arenas created yet."));
            return;
        }

        sender.sendMessage(MessageFormatter.header("Arenas"));
        for (Arena arena : arenas) {
            String centerInfo = arena.getCenter() != null ? "center set" : "no center";
            Component line = Component.text()
                    .append(Component.text("  " + arena.getName(), EmaraTheme.WARNING))
                    .append(Component.text(" (" + arena.getState() + ", " + centerInfo + ")", EmaraTheme.MUTED))
                    .build();
            sender.sendMessage(line);
        }
    }

    private void handleArenaDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague arena delete <name>")));
            return;
        }

        String name = args[2];
        if (arenaManager.deleteArena(name)) {
            sender.sendMessage(MessageFormatter.success("Arena '" + name + "' deleted."));
        } else {
            sender.sendMessage(MessageFormatter.error("Arena not found: " + name));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague info <tournament>")));
            return;
        }

        String name = args[1];
        var nameResult = InputValidator.validateTournamentName(name);
        if (!nameResult.isValid()) {
            sender.sendMessage(MessageFormatter.error(nameResult.getErrorMessage()));
            return;
        }

        Optional<Tournament> tournament = tournamentManager.getTournament(name);
        if (tournament.isEmpty()) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", name)));
            return;
        }

        Tournament t = tournament.get();
        String friendlyStatus = getFriendlyStatus(t.state());
        sender.sendMessage(messages.get("tournament-info", Map.of(
                "name", t.name(),
                "mode", t.mode(),
                "status", friendlyStatus
        )));
    }

    private String getFriendlyStatus(TournamentState state) {
        return switch (state) {
            case REGISTRATION -> "Open for Registration";
            case STARTING -> "Starting Soon";
            case IN_PROGRESS -> "Match in Progress";
            case ENDED -> "Tournament Ended";
            case CANCELLED -> "Tournament Cancelled";
        };
    }

    private void handleReload(CommandSender sender) {
        messages.reload();
        sender.sendMessage(messages.get("reload-success"));
    }

    private void handleSpectate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(messages.get("invalid-usage", Map.of("usage", "/emaraleague spectate <tournament>")));
            return;
        }

        String tournamentName = args[1];
        if (!tournamentManager.exists(tournamentName)) {
            sender.sendMessage(messages.get("tournament-not-found", Map.of("name", tournamentName)));
            return;
        }

        com.emaralabs.emaraleague.core.match.MatchEngine matchEngine =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchEngine();

        // Find active match
        List<com.emaralabs.emaraleague.core.tournament.Match> activeMatches = matchEngine.getMatchesByState(
                tournamentName, com.emaralabs.emaraleague.core.tournament.MatchState.INGAME);
        if (activeMatches.isEmpty()) {
            activeMatches = matchEngine.getMatchesByState(tournamentName, com.emaralabs.emaraleague.core.tournament.MatchState.STARTING);
        }
        if (activeMatches.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("No active match in this tournament."));
            return;
        }

        com.emaralabs.emaraleague.core.tournament.Match match = activeMatches.get(0);

        // Get arena for teleport
        UUID arenaId = matchEngine.getMatchToArena().get(match.id());
        if (arenaId == null) {
            sender.sendMessage(MessageFormatter.error("Match has no arena assigned yet."));
            return;
        }

        Optional<Arena> arena = arenaManager.getArena(arenaId);
        if (arena.isEmpty() || arena.get().getCenter() == null) {
            sender.sendMessage(MessageFormatter.error("Arena not ready for spectating."));
            return;
        }

        // Add spectator
        matchEngine.getSpectatorManager().addSpectator(player.getUniqueId(), match.id(), arenaId);
        player.teleport(arena.get().getCenter());
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        sender.sendMessage(MessageFormatter.success("You are now spectating the match."));
        sender.sendMessage(MessageFormatter.muted("Use /emaraleague spectate off to stop."));
    }

    private void handleRejoin(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("player-only"));
            return;
        }

        com.emaralabs.emaraleague.core.match.MatchEngine matchEngine =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getMatchEngine();

        // Check if player is in an active match (via session)
        com.emaralabs.emaraleague.core.player.PlayerSessionManager sessions =
                com.emaralabs.emaraleague.EmaraLeaguePlugin.getInstance().getPlayerSessionManager();

        UUID playerId = player.getUniqueId();
        Optional<UUID> matchId = sessions.getMatchId(playerId);

        if (matchId.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("You are not in an active match."));
            return;
        }

        Optional<com.emaralabs.emaraleague.core.tournament.Match> match = matchEngine.getMatch(matchId.get());
        if (match.isEmpty()) {
            sender.sendMessage(MessageFormatter.error("Match not found."));
            return;
        }

        if (match.get().state() != com.emaralabs.emaraleague.core.tournament.MatchState.INGAME) {
            sender.sendMessage(MessageFormatter.error("Match is not in progress."));
            return;
        }

        // Check grace period (5 minutes = 300 seconds)
        long disconnectTime = sessions.getSession(playerId).map(s -> s.getDisconnectTime()).orElse(0L);
        if (disconnectTime > 0 && (System.currentTimeMillis() - disconnectTime) > 300_000) {
            sender.sendMessage(MessageFormatter.error("Rejoin grace period has expired."));
            return;
        }

        // Teleport back to arena
        UUID arenaId = matchEngine.getMatchToArena().get(matchId.get());
        if (arenaId == null) {
            sender.sendMessage(MessageFormatter.error("Arena not found."));
            return;
        }

        Optional<Arena> arena = arenaManager.getArena(arenaId);
        if (arena.isEmpty() || arena.get().getCenter() == null) {
            sender.sendMessage(MessageFormatter.error("Arena not ready."));
            return;
        }

        player.teleport(arena.get().getCenter());
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        sessions.markReconnected(playerId);
        sender.sendMessage(MessageFormatter.success("You have rejoined the match!"));
    }

    private void sendHelp(CommandSender sender) {
        // Header with gradient effect
        sender.sendMessage(Component.text("✦ ", EmaraTheme.PRIMARY)
                .append(Component.text("EmaraLeague", EmaraTheme.PRIMARY, TextDecoration.BOLD))
                .append(Component.text(" ✦", EmaraTheme.PRIMARY)));
        sender.sendMessage(Component.text("Tournament Commands", EmaraTheme.MUTED));
        sender.sendMessage(Component.empty());

        // Group commands by category
        List<SubCommand> tournamentCmds = new ArrayList<>();
        List<SubCommand> teamCmds = new ArrayList<>();
        List<SubCommand> arenaCmds = new ArrayList<>();
        List<SubCommand> infoCmds = new ArrayList<>();
        List<SubCommand> adminCmds = new ArrayList<>();

        for (SubCommand sub : SUB_COMMANDS) {
            if (!sender.hasPermission(sub.permission) && !sender.hasPermission("emaraleague.admin")) {
                continue;
            }
            String name = sub.name.toLowerCase();
            if (name.equals("create") || name.equals("delete") || name.equals("join") || name.equals("leave") || name.equals("start") || name.equals("info") || name.equals("spectate")) {
                tournamentCmds.add(sub);
            } else if (name.equals("team") || name.equals("rejoin")) {
                teamCmds.add(sub);
            } else if (name.equals("arena")) {
                arenaCmds.add(sub);
            } else if (name.equals("history") || name.equals("stats")) {
                infoCmds.add(sub);
            } else if (name.equals("reload")) {
                adminCmds.add(sub);
            }
        }

        // Display by category with icons
        if (!tournamentCmds.isEmpty()) {
            sender.sendMessage(Component.text("🏆 ", EmaraTheme.WARNING)
                    .append(Component.text("Tournament", EmaraTheme.WARNING, TextDecoration.BOLD)));
            for (SubCommand sub : tournamentCmds) {
                sendHelpLine(sender, sub);
            }
            sender.sendMessage(Component.empty());
        }

        if (!teamCmds.isEmpty()) {
            sender.sendMessage(Component.text("👥 ", EmaraTheme.TEAM_A)
                    .append(Component.text("Teams", EmaraTheme.TEAM_A, TextDecoration.BOLD)));
            for (SubCommand sub : teamCmds) {
                sendHelpLine(sender, sub);
            }
            sender.sendMessage(Component.empty());
        }

        if (!arenaCmds.isEmpty()) {
            sender.sendMessage(Component.text("🏟 ", EmaraTheme.ARENA)
                    .append(Component.text("Arenas", EmaraTheme.ARENA, TextDecoration.BOLD)));
            for (SubCommand sub : arenaCmds) {
                sendHelpLine(sender, sub);
            }
            sender.sendMessage(Component.empty());
        }

        if (!infoCmds.isEmpty()) {
            sender.sendMessage(Component.text("📊 ", EmaraTheme.INFO)
                    .append(Component.text("Info", EmaraTheme.INFO, TextDecoration.BOLD)));
            for (SubCommand sub : infoCmds) {
                sendHelpLine(sender, sub);
            }
            sender.sendMessage(Component.empty());
        }

        if (!adminCmds.isEmpty()) {
            sender.sendMessage(Component.text("⚙ ", EmaraTheme.ACCENT)
                    .append(Component.text("Admin", EmaraTheme.ACCENT, TextDecoration.BOLD)));
            for (SubCommand sub : adminCmds) {
                sendHelpLine(sender, sub);
            }
        }

        // Footer
        sender.sendMessage(Component.empty());
        sender.sendMessage(Component.text("Use ", EmaraTheme.MUTED)
                .append(Component.text("/el <command>", EmaraTheme.WARNING))
                .append(Component.text(" to get started", EmaraTheme.MUTED)));
    }

    private void sendHelpLine(CommandSender sender, SubCommand sub) {
        Component line = Component.text()
                .append(Component.text("  ▸ ", EmaraTheme.SEPARATOR))
                .append(Component.text(sub.usage, EmaraTheme.WARNING))
                .append(Component.text(" — ", EmaraTheme.MUTED))
                .append(Component.text(sub.description, EmaraTheme.TEXT))
                .build();
        sender.sendMessage(line);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterCompletions(getVisibleSubCommands(sender), args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "create" -> filterCompletions(List.of("<name>"), args[1]);
                case "delete", "join", "start", "info", "team", "spectate" -> filterCompletions(getTournamentNames(), args[1]);
                case "arena" -> filterCompletions(List.of("create", "setcenter", "setlobby", "setspawn", "list", "delete"), args[1]);
                default -> Collections.emptyList();
            };
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                return filterCompletions(GAME_MODES, args[2]);
            }
            if (args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("join")) {
                return filterCompletions(getTournamentNames(), args[2]);
            }
            if (args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("list")) {
                return filterCompletions(getTournamentNames(), args[2]);
            }
            if (args[0].equalsIgnoreCase("arena")) {
                return filterCompletions(getArenaNames(), args[2]);
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("join")) {
            return filterCompletions(getTeamNames(args[2]), args[3]);
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> options, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }

    private List<String> getVisibleSubCommands(CommandSender sender) {
        List<String> visible = new ArrayList<>();
        for (SubCommand sub : SUB_COMMANDS) {
            if (sender.hasPermission(sub.permission) || sender.hasPermission("emaraleague.admin")) {
                visible.add(sub.name);
            }
        }
        return visible;
    }

    private List<String> getTournamentNames() {
        return tournamentManager.getTournaments().stream()
                .map(Tournament::name)
                .toList();
    }

    private List<String> getTeamNames(String tournamentName) {
        return tournamentManager.getTournament(tournamentName)
                .map(t -> t.teams().stream()
                        .map(Team::name)
                        .toList())
                .orElse(List.of());
    }

    private List<String> getArenaNames() {
        return arenaManager.getArenas().stream()
                .map(Arena::getName)
                .toList();
    }

    private boolean hasPermission(CommandSender sender, String subCommand) {
        String permission = "emaraleague." + subCommand.toLowerCase();
        return sender.hasPermission(permission) || sender.hasPermission("emaraleague.admin");
    }

    public MessageRegistry getMessages() {
        return messages;
    }

    private record SubCommand(String name, String usage, String description, String permission) {}
}
