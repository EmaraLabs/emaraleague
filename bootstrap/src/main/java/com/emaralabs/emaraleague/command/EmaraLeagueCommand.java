package com.emaralabs.emaraleague.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class EmaraLeagueCommand implements CommandExecutor, TabCompleter {

    private final Plugin plugin;

    public EmaraLeagueCommand(Plugin plugin) {
        this.plugin = plugin;
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
            sender.sendMessage("EmaraLeague v1.0 - Use /emaraleague help");
            return true;
        }

        if (!hasPermission(sender, args[0])) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage("Usage: /emaraleague create <name> <mode>");
                    return true;
                }
                sender.sendMessage("Creating tournament: " + args[1]);
            }
            case "join" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /emaraleague join <tournament>");
                    return true;
                }
                sender.sendMessage("Joining tournament: " + args[1]);
            }
            case "leave" -> sender.sendMessage("Leaving tournament");
            case "start" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /emaraleague start <tournament>");
                    return true;
                }
                sender.sendMessage("Starting tournament: " + args[1]);
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /emaraleague info <tournament>");
                    return true;
                }
                sender.sendMessage("Tournament info: " + args[1]);
            }
            case "help" -> sender.sendMessage("Help menu");
            default -> sender.sendMessage("Unknown command. Use /emaraleague help");
        }

        return true;
    }

    private boolean hasPermission(CommandSender sender, String subCommand) {
        String permission = "emaraleague." + subCommand.toLowerCase();
        return sender.hasPermission(permission) || sender.hasPermission("emaraleague.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("create", "join", "leave", "start", "info", "help");
        }
        return new ArrayList<>();
    }
}
