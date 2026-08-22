package com.emaralabs.emaraleague.core.integration;

import com.emaralabs.emaraleague.core.arena.ArenaManager;
import com.emaralabs.emaraleague.core.arena.ArenaState;
import com.emaralabs.emaraleague.core.bracket.Bracket;
import com.emaralabs.emaraleague.core.bracket.SingleEliminationBracket;
import com.emaralabs.emaraleague.core.game.GameMode;
import com.emaralabs.emaraleague.core.game.GameModeRegistry;
import com.emaralabs.emaraleague.core.game.WinCondition;
import com.emaralabs.emaraleague.core.match.MatchEngine;
import com.emaralabs.emaraleague.core.match.WinConditionEvaluator;
import com.emaralabs.emaraleague.core.player.PlayerSessionManager;
import com.emaralabs.emaraleague.core.tournament.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class FullTournamentFlowTest {

    private TournamentManager tournaments;
    private ArenaManager arenas;
    private MatchEngine matchEngine;
    private GameModeRegistry gameModes;
    private PlayerSessionManager sessions;
    private WinConditionEvaluator winEvaluator;
    private TestGameMode testMode;

    @BeforeEach
    void setUp() {
        tournaments = new TournamentManager();
        arenas = new ArenaManager();
        matchEngine = new MatchEngine(tournaments, arenas);
        gameModes = new GameModeRegistry();
        sessions = new PlayerSessionManager();
        winEvaluator = new WinConditionEvaluator(sessions);

        testMode = new TestGameMode();
        gameModes.register(testMode);
        matchEngine.setGameModeRegistry(gameModes);
    }

    // ── Full Flow: Duels 1v1 ────────────────────────────────────────

    @Test
    void fullTournamentFlow_twoTeams_completeCycle() {
        // 1. Create tournament
        Tournament t = tournaments.createTournament("SummerCup", "test", BracketType.SINGLE_ELIMINATION);
        assertEquals(TournamentState.REGISTRATION, t.state());
        assertEquals(0, t.teams().size());

        // 2. Add teams
        Team alpha = new Team("Alpha", 1);
        Team beta = new Team("Beta", 2);
        tournaments.addTeam("SummerCup", alpha);
        tournaments.addTeam("SummerCup", beta);
        assertEquals(2, tournaments.getTeamCount("SummerCup"));

        // 3. Assign players
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        tournaments.assignPlayerToTeam("SummerCup", alpha.id(), p1);
        tournaments.assignPlayerToTeam("SummerCup", beta.id(), p2);

        assertEquals(1, tournaments.getTeamPlayerCount("SummerCup", alpha.id()));
        assertEquals(1, tournaments.getTeamPlayerCount("SummerCup", beta.id()));

        // Verify player→team mapping
        Optional<Team> p1Team = tournaments.getTeamForPlayer("SummerCup", p1);
        assertTrue(p1Team.isPresent());
        assertEquals("Alpha", p1Team.get().name());

        // 4. Transition to STARTING
        Tournament starting = tournaments.transitionState("SummerCup", TournamentState.STARTING);
        assertEquals(TournamentState.STARTING, starting.state());

        // 5. Generate bracket
        Bracket bracket = matchEngine.generateBracket("SummerCup", new SingleEliminationBracket());
        assertEquals(1, bracket.getTotalMatches());

        // 6. Start match
        Match match = bracket.getMatches().get(0);
        Match started = matchEngine.startMatch(match.id());
        assertEquals(MatchState.STARTING, started.state());

        // Verify game mode hook was called
        assertTrue(testMode.matchStartCalled);

        // 7. Begin play (countdown complete)
        Match inGame = matchEngine.beginPlay(match.id());
        assertEquals(MatchState.INGAME, inGame.state());

        // 8. Eliminate one team
        testMode.eliminateTeam(alpha.id());

        // 9. Check win condition
        Optional<Team> winner = winEvaluator.evaluate(inGame, testMode);
        assertTrue(winner.isPresent());
        assertEquals(beta.id(), winner.get().id());

        // 10. End match
        Match ended = matchEngine.endMatch(match.id(), beta);
        assertEquals(MatchState.ENDED, ended.state());
        assertEquals(beta.id(), ended.winner().id());

        // Verify game mode hook was called
        assertTrue(testMode.matchEndCalled);
        assertEquals(beta.id(), testMode.lastWinner.id());
    }

    // ── Full Flow: 4 Teams with Bracket Advancement ─────────────────

    @Test
    void fullTournamentFlow_fourTeams_bracketAdvancement() {
        // Create tournament
        tournaments.createTournament("WorldCup", "test", BracketType.SINGLE_ELIMINATION);

        // Add 4 teams
        Team[] teams = new Team[4];
        for (int i = 0; i < 4; i++) {
            teams[i] = new Team("Team" + (i + 1), i + 1);
            tournaments.addTeam("WorldCup", teams[i]);
        }
        assertEquals(4, tournaments.getTeamCount("WorldCup"));

        // Assign players
        UUID[] players = new UUID[4];
        for (int i = 0; i < 4; i++) {
            players[i] = UUID.randomUUID();
            tournaments.assignPlayerToTeam("WorldCup", teams[i].id(), players[i]);
        }

        // Transition and generate bracket
        tournaments.transitionState("WorldCup", TournamentState.STARTING);
        Bracket bracket = matchEngine.generateBracket("WorldCup", new SingleEliminationBracket());

        // Single elimination with 4 teams = 3 total matches (2 first round + 1 final)
        assertEquals(3, bracket.getTotalMatches());

        // Only 2 first-round matches have both teams assigned (final is TBD)
        assertEquals(2, matchEngine.getMatchesByState("WorldCup", MatchState.PENDING).size());
    }

    // ── Arena State Machine Integration ─────────────────────────────

    @Test
    void arena_fullStateCycle() {
        arenas.createArena("MainArena");
        assertEquals(ArenaState.LOBBY, arenas.getArena("MainArena").get().getState());

        arenas.transitionArena("MainArena", ArenaState.STARTING);
        assertEquals(ArenaState.STARTING, arenas.getArena("MainArena").get().getState());

        arenas.transitionArena("MainArena", ArenaState.INGAME);
        assertEquals(ArenaState.INGAME, arenas.getArena("MainArena").get().getState());

        arenas.transitionArena("MainArena", ArenaState.ENDING);
        assertEquals(ArenaState.ENDING, arenas.getArena("MainArena").get().getState());

        arenas.transitionArena("MainArena", ArenaState.RESETTING);
        assertEquals(ArenaState.RESETTING, arenas.getArena("MainArena").get().getState());

        arenas.transitionArena("MainArena", ArenaState.LOBBY);
        assertEquals(ArenaState.LOBBY, arenas.getArena("MainArena").get().getState());
    }

    // ── Match Engine Queries ────────────────────────────────────────

    @Test
    void matchEngine_queries_afterOperations() {
        tournaments.createTournament("Cup", "test", BracketType.SINGLE_ELIMINATION);
        tournaments.addTeam("Cup", new Team("Alpha", 1));
        tournaments.addTeam("Cup", new Team("Beta", 2));
        tournaments.transitionState("Cup", TournamentState.STARTING);

        Bracket bracket = matchEngine.generateBracket("Cup", new SingleEliminationBracket());
        Match match = bracket.getMatches().get(0);

        // Before start
        assertEquals(1, matchEngine.getMatchesByState("Cup", MatchState.PENDING).size());
        assertEquals(0, matchEngine.getActiveMatchCount("Cup"));

        // After start
        matchEngine.startMatch(match.id());
        assertEquals(1, matchEngine.getMatchesByState("Cup", MatchState.STARTING).size());
        assertEquals(1, matchEngine.getActiveMatchCount("Cup"));

        // After begin play
        matchEngine.beginPlay(match.id());
        assertEquals(1, matchEngine.getMatchesByState("Cup", MatchState.INGAME).size());
        assertEquals(1, matchEngine.getActiveMatchCount("Cup"));

        // After end
        matchEngine.endMatch(match.id(), match.teamA());
        assertEquals(1, matchEngine.getMatchesByState("Cup", MatchState.ENDED).size());
        assertEquals(0, matchEngine.getActiveMatchCount("Cup"));
    }

    // ── Persistence Integration ─────────────────────────────────────

    @Test
    void tournamentPersistence_saveAndRestore() {
        // Create in-memory persistence
        InMemoryTournamentPersistence persistence = new InMemoryTournamentPersistence();

        TournamentManager persistentManager = new TournamentManager();
        persistentManager.setPersistence(persistence);

        // Create and populate tournament
        persistentManager.createTournament("PersistCup", "test", BracketType.SINGLE_ELIMINATION);
        persistentManager.addTeam("PersistCup", new Team("Alpha", 1));
        persistentManager.addTeam("PersistCup", new Team("Beta", 2));

        // Create new manager and load from persistence
        TournamentManager loadedManager = new TournamentManager();
        loadedManager.setPersistence(persistence);
        loadedManager.loadFromDatabase();

        // Verify data restored
        assertEquals(1, loadedManager.count());
        assertTrue(loadedManager.exists("PersistCup"));
        assertEquals(2, loadedManager.getTeamCount("PersistCup"));
    }

    // ── In-Memory Persistence Stub ──────────────────────────────────

    static class InMemoryTournamentPersistence implements TournamentPersistence {
        private final Map<UUID, Tournament> store = new ConcurrentHashMap<>();

        @Override
        public CompletableFuture<Tournament> save(Tournament tournament) {
            store.put(tournament.id(), tournament);
            return CompletableFuture.completedFuture(tournament);
        }

        @Override
        public CompletableFuture<Optional<Tournament>> findById(UUID id) {
            return CompletableFuture.completedFuture(Optional.ofNullable(store.get(id)));
        }

        @Override
        public CompletableFuture<List<Tournament>> findAll() {
            return CompletableFuture.completedFuture(List.copyOf(store.values()));
        }

        @Override
        public CompletableFuture<Void> delete(UUID id) {
            store.remove(id);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Tournament> update(Tournament tournament) {
            store.put(tournament.id(), tournament);
            return CompletableFuture.completedFuture(tournament);
        }
    }

    // ── Test Game Mode Stub ─────────────────────────────────────────

    static class TestGameMode implements GameMode {
        boolean matchStartCalled = false;
        boolean matchEndCalled = false;
        Team lastWinner = null;
        private final Set<UUID> eliminatedTeams = new HashSet<>();
        private final Map<UUID, UUID> playerToTeam = new HashMap<>();

        void eliminateTeam(UUID teamId) {
            eliminatedTeams.add(teamId);
        }

        void assignPlayerToTeam(UUID playerId, UUID teamId) {
            playerToTeam.put(playerId, teamId);
        }

        @Override
        public String getId() { return "test"; }

        @Override
        public String getDisplayName() { return "Test Mode"; }

        @Override
        public int getMinPlayers() { return 2; }

        @Override
        public int getMaxPlayers() { return 16; }

        @Override
        public void onMatchStart(Match match) {
            matchStartCalled = true;
        }

        @Override
        public void onMatchTick(Match match) {}

        @Override
        public void onMatchEnd(Match match, Team winner) {
            matchEndCalled = true;
            lastWinner = winner;
        }

        @Override
        public WinCondition getWinCondition() { return WinCondition.LAST_TEAM_STANDING; }

        @Override
        public boolean isTeamEliminated(UUID teamId) {
            return eliminatedTeams.contains(teamId);
        }

        @Override
        public Optional<UUID> getTeamForPlayer(UUID playerId) {
            return Optional.ofNullable(playerToTeam.get(playerId));
        }
    }
}
