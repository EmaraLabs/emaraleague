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

        switch (args[0].toLowerCase()) {
            case "create" -> sender.sendMessage("Create tournament");
            case "join" -> sender.sendMessage("Join tournament");
            case "leave" -> sender.sendMessage("Leave tournament");
            case "start" -> sender.sendMessage("Start tournament");
            case "info" -> sender.sendMessage("Tournament info");
            case "help" -> sender.sendMessage("Help menu");
            default -> sender.sendMessage("Unknown command. Use /emaraleague help");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("create", "join", "leave", "start", "info", "help");
        }
        return new ArrayList<>();
    }
}
