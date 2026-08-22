package com.emaralabs.emaraleague.command;

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

    private static final List<String> GAME_MODES = List.of(
            "duels", "spleef", "sumo", "tnt-run", "parkour", "capture-the-flag"
    );

    private static final List<SubCommand> SUB_COMMANDS = List.of(
            new SubCommand("create", "/emaraleague create <name> <mode>", "Create a new tournament", "emaraleague.create"),
            new SubCommand("join", "/emaraleague join <tournament>", "Join a tournament", "emaraleague.play"),
            new SubCommand("leave", "/emaraleague leave", "Leave your current tournament", "emaraleague.play"),
            new SubCommand("team", "/emaraleague team <join|leave|list>", "Manage teams", "emaraleague.play"),
            new SubCommand("start", "/emaraleague start <tournament>", "Start a tournament", "emaraleague.admin"),
            new SubCommand("info", "/emaraleague info <tournament>", "View tournament details", "emaraleague.use"),
            new SubCommand("help", "/emaraleague help", "Show this menu", "emaraleague.use"),
            new SubCommand("reload", "/emaraleague reload", "Reload configuration", "emaraleague.admin")
    );

    public EmaraLeagueCommand(Plugin plugin, TournamentManager tournamentManager) {
        this.plugin = plugin;
        this.tournamentManager = tournamentManager;
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

        switch (sub) {
            case "create" -> handleCreate(sender, args);
            case "join" -> handleJoin(sender, args);
            case "leave" -> handleLeave(sender);
            case "team" -> handleTeam(sender, args);
            case "start" -> handleStart(sender, args);
            case "info" -> handleInfo(sender, args);
            case "help" -> sendHelp(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(messages.get("unknown-command"));
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

        sender.sendMessage(messages.get("tournament-joined", Map.of("name", name)));
    }

    private void handleLeave(CommandSender sender) {
        sender.sendMessage(messages.get("tournament-left"));
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

        try {
            tournamentManager.transitionState(name, TournamentState.STARTING);
            sender.sendMessage(messages.get("tournament-started", Map.of("name", name)));
        } catch (IllegalArgumentException | IllegalStateException e) {
            sender.sendMessage(MessageFormatter.error(e.getMessage()));
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
        sender.sendMessage(messages.get("tournament-info", Map.of(
                "name", t.name(),
                "mode", t.mode(),
                "status", t.state().name()
        )));
    }

    private void handleReload(CommandSender sender) {
        messages.reload();
        sender.sendMessage(messages.get("reload-success"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messages.get("help-header"));
        sender.sendMessage(Component.empty());

        for (SubCommand sub : SUB_COMMANDS) {
            if (sender.hasPermission(sub.permission) || sender.hasPermission("emaraleague.admin")) {
                Component line = Component.text()
                        .append(Component.text("  " + sub.usage, EmaraTheme.WARNING))
                        .append(Component.text(" — ", EmaraTheme.MUTED))
                        .append(Component.text(sub.description, EmaraTheme.INFO))
                        .build();
                sender.sendMessage(line);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return filterCompletions(getVisibleSubCommands(sender), args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "create" -> filterCompletions(List.of("<name>"), args[1]);
                case "join", "start", "info", "team" -> filterCompletions(getTournamentNames(), args[1]);
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

    private boolean hasPermission(CommandSender sender, String subCommand) {
        String permission = "emaraleague." + subCommand.toLowerCase();
        return sender.hasPermission(permission) || sender.hasPermission("emaraleague.admin");
    }

    public MessageRegistry getMessages() {
        return messages;
    }

    private record SubCommand(String name, String usage, String description, String permission) {}
}
