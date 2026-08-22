package com.emaralabs.emaraleague.core.performance;

import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket;
import com.emaralabs.emaraleague.core.tournament.Team;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceTest {

    @Test
    void testBracketGenerationPerformance() {
        SingleEliminationBracket bracketGen = new SingleEliminationBracket();
        List<Team> teams = new ArrayList<>();
        
        for (int i = 0; i < 64; i++) {
            teams.add(new Team("Team " + i, i + 1));
        }
        
        long startTime = System.nanoTime();
        Bracket bracket = bracketGen.generate(teams);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertNotNull(bracket);
        assertTrue(durationMs < 100, "Bracket generation took " + durationMs + "ms, expected < 100ms");
    }

    @Test
    void testMultipleBracketsPerformance() {
        SingleEliminationBracket bracketGen = new SingleEliminationBracket();
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 100; i++) {
            List<Team> teams = new ArrayList<>();
            for (int j = 0; j < 16; j++) {
                teams.add(new Team("Team " + j, j + 1));
            }
            bracketGen.generate(teams);
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        assertTrue(durationMs < 1000, "100 bracket generations took " + durationMs + "ms, expected < 1000ms");
    }

    @Test
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        
        SingleEliminationBracket bracketGen = new SingleEliminationBracket();
        List<Bracket> brackets = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            List<Team> teams = new ArrayList<>();
            for (int j = 0; j < 32; j++) {
                teams.add(new Team("Team " + j, j + 1));
            }
            brackets.add(bracketGen.generate(teams));
        }
        
        long endMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsedMB = (endMemory - startMemory) / (1024 * 1024);
        
        assertTrue(memoryUsedMB < 100, "Memory usage: " + memoryUsedMB + "MB, expected < 100MB");
        assertEquals(100, brackets.size());
    }
}
