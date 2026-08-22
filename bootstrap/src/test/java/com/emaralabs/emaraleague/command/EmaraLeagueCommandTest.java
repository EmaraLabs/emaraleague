package com.emaralabs.emaraleague.command;

import com.emaralabs.emaraleague.core.tournament.TournamentManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmaraLeagueCommandTest {

    private Plugin mockPlugin;
    private TournamentManager tournamentManager;
    private EmaraLeagueCommand cmd;

    @BeforeEach
    void setUp() {
        mockPlugin = mock(Plugin.class);
        when(mockPlugin.getDataFolder()).thenReturn(new File("build/tmp/test-cmd"));
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));
        tournamentManager = new TournamentManager();
        cmd = new EmaraLeagueCommand(mockPlugin, tournamentManager);
    }

    @Test
    void testCommandCreation() {
        assertNotNull(cmd);
    }

    @Test
    void testCommandName() {
        assertEquals("emaraleague", cmd.getName());
    }

    @Test
    void testCommandAliases() {
        assertTrue(cmd.getAliases().contains("el"));
        assertTrue(cmd.getAliases().contains("league"));
    }

    @Test
    void testCommandDescription() {
        assertEquals("EmaraLeague tournament management", cmd.getDescription());
    }

    @Test
    void handleCreate_createsTournamentInManager() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"create", "SummerCup", "duels"});

        assertTrue(tournamentManager.exists("SummerCup"));
        assertEquals(1, tournamentManager.count());
    }

    @Test
    void handleCreate_duplicateName_sendsError() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);
        tournamentManager.createTournament("SummerCup", "duels", com.emaralabs.emaraleague.core.tournament.BracketType.SINGLE_ELIMINATION);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"create", "SummerCup", "duels"});

        verify(sender).sendMessage(any(net.kyori.adventure.text.Component.class));
        assertEquals(1, tournamentManager.count());
    }

    @Test
    void handleJoin_sendsJoinedMessage() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);
        tournamentManager.createTournament("SummerCup", "duels", com.emaralabs.emaraleague.core.tournament.BracketType.SINGLE_ELIMINATION);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"join", "SummerCup"});

        verify(sender).sendMessage(any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void handleInfo_existingTournament_sendsInfo() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);
        tournamentManager.createTournament("SummerCup", "duels", com.emaralabs.emaraleague.core.tournament.BracketType.SINGLE_ELIMINATION);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"info", "SummerCup"});

        verify(sender).sendMessage(any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void handleInfo_nonExistentTournament_sendsNotFound() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"info", "NonExistent"});

        verify(sender).sendMessage(any(net.kyori.adventure.text.Component.class));
    }

    @Test
    void handleStart_transitionsToStarting() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);
        tournamentManager.createTournament("SummerCup", "duels", com.emaralabs.emaraleague.core.tournament.BracketType.SINGLE_ELIMINATION);

        cmd.onCommand(sender, null, "emaraleague", new String[]{"start", "SummerCup"});

        assertEquals(com.emaralabs.emaraleague.core.tournament.TournamentState.STARTING,
                tournamentManager.getTournament("SummerCup").get().state());
    }

    @Test
    void tabComplete_createArg2_suggestsName() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);

        var completions = cmd.onTabComplete(sender, null, "emaraleague", new String[]{"create", ""});
        assertTrue(completions.contains("<name>"));
    }

    @Test
    void tabComplete_createArg3_suggestsGameModes() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);

        var completions = cmd.onTabComplete(sender, null, "emaraleague", new String[]{"create", "MyCup", ""});
        assertTrue(completions.contains("duels"));
        assertTrue(completions.contains("spleef"));
    }

    @Test
    void tabComplete_joinArg2_suggestsTournamentNames() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission(anyString())).thenReturn(true);
        tournamentManager.createTournament("SummerCup", "duels", com.emaralabs.emaraleague.core.tournament.BracketType.SINGLE_ELIMINATION);

        var completions = cmd.onTabComplete(sender, null, "emaraleague", new String[]{"join", ""});
        assertTrue(completions.contains("SummerCup"));
    }
}
