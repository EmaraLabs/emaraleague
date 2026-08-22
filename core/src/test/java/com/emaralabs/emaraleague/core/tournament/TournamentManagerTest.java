package com.emaralabs.emaraleague.core.tournament;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TournamentManagerTest {

    // ── CRUD ────────────────────────────────────────────────────────

    @Test
    void createTournament_returnsNewTournament() {
        TournamentManager manager = new TournamentManager();
        Tournament t = manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        assertNotNull(t);
        assertEquals("SummerCup", t.name());
        assertEquals("duels", t.mode());
        assertEquals(BracketType.SINGLE_ELIMINATION, t.bracketType());
        assertEquals(TournamentState.REGISTRATION, t.state());
        assertNotNull(t.id());
    }

    @Test
    void createTournament_duplicateName_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        assertThrows(IllegalArgumentException.class,
                () -> manager.createTournament("SummerCup", "spleef", BracketType.ROUND_ROBIN));
    }

    @Test
    void getTournament_byName_returnsTournament() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        Optional<Tournament> found = manager.getTournament("SummerCup");
        assertTrue(found.isPresent());
        assertEquals("SummerCup", found.get().name());
    }

    @Test
    void getTournament_byName_caseInsensitive() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        assertTrue(manager.getTournament("summercup").isPresent());
        assertTrue(manager.getTournament("SUMMERCUP").isPresent());
    }

    @Test
    void getTournament_byId_returnsTournament() {
        TournamentManager manager = new TournamentManager();
        Tournament created = manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        Optional<Tournament> found = manager.getTournament(created.id());
        assertTrue(found.isPresent());
        assertEquals(created.id(), found.get().id());
    }

    @Test
    void getTournament_notFound_returnsEmpty() {
        TournamentManager manager = new TournamentManager();
        assertTrue(manager.getTournament("NonExistent").isEmpty());
        assertTrue(manager.getTournament(UUID.randomUUID()).isEmpty());
    }

    @Test
    void getTournaments_returnsAll() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup1", "duels", BracketType.SINGLE_ELIMINATION);
        manager.createTournament("Cup2", "spleef", BracketType.ROUND_ROBIN);
        List<Tournament> all = manager.getTournaments();
        assertEquals(2, all.size());
    }

    @Test
    void getTournamentsByState_filtersCorrectly() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup1", "duels", BracketType.SINGLE_ELIMINATION);
        manager.createTournament("Cup2", "spleef", BracketType.ROUND_ROBIN);
        List<Tournament> registration = manager.getTournamentsByState(TournamentState.REGISTRATION);
        assertEquals(2, registration.size());
        List<Tournament> inProgress = manager.getTournamentsByState(TournamentState.IN_PROGRESS);
        assertEquals(0, inProgress.size());
    }

    @Test
    void deleteTournament_removesFromBothMaps() {
        TournamentManager manager = new TournamentManager();
        Tournament t = manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        assertTrue(manager.deleteTournament("SummerCup"));
        assertTrue(manager.getTournament("SummerCup").isEmpty());
        assertTrue(manager.getTournament(t.id()).isEmpty());
        assertEquals(0, manager.count());
    }

    @Test
    void deleteTournament_notFound_returnsFalse() {
        TournamentManager manager = new TournamentManager();
        assertFalse(manager.deleteTournament("NonExistent"));
    }

    @Test
    void exists_returnsTrueForExisting() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("SummerCup", "duels", BracketType.SINGLE_ELIMINATION);
        assertTrue(manager.exists("SummerCup"));
        assertTrue(manager.exists("summercup"));
        assertFalse(manager.exists("NonExistent"));
    }

    @Test
    void count_reflectsCurrentSize() {
        TournamentManager manager = new TournamentManager();
        assertEquals(0, manager.count());
        manager.createTournament("Cup1", "duels", BracketType.SINGLE_ELIMINATION);
        assertEquals(1, manager.count());
        manager.createTournament("Cup2", "spleef", BracketType.ROUND_ROBIN);
        assertEquals(2, manager.count());
        manager.deleteTournament("Cup1");
        assertEquals(1, manager.count());
    }

    // ── State Machine ───────────────────────────────────────────────

    @Test
    void transitionState_registrationToStarting_succeeds() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Tournament updated = manager.transitionState("Cup", TournamentState.STARTING);
        assertEquals(TournamentState.STARTING, updated.state());
    }

    @Test
    void transitionState_startingToInProgress_succeeds() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        manager.transitionState("Cup", TournamentState.STARTING);
        Tournament updated = manager.transitionState("Cup", TournamentState.IN_PROGRESS);
        assertEquals(TournamentState.IN_PROGRESS, updated.state());
    }

    @Test
    void transitionState_inProgressToEnded_succeeds() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        manager.transitionState("Cup", TournamentState.STARTING);
        manager.transitionState("Cup", TournamentState.IN_PROGRESS);
        Tournament updated = manager.transitionState("Cup", TournamentState.ENDED);
        assertEquals(TournamentState.ENDED, updated.state());
    }

    @Test
    void transitionState_registrationToInProgress_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertThrows(IllegalStateException.class,
                () -> manager.transitionState("Cup", TournamentState.IN_PROGRESS));
    }

    @Test
    void transitionState_registrationToEnded_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertThrows(IllegalStateException.class,
                () -> manager.transitionState("Cup", TournamentState.ENDED));
    }

    @Test
    void transitionState_endedToAny_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        manager.transitionState("Cup", TournamentState.STARTING);
        manager.transitionState("Cup", TournamentState.IN_PROGRESS);
        manager.transitionState("Cup", TournamentState.ENDED);
        assertThrows(IllegalStateException.class,
                () -> manager.transitionState("Cup", TournamentState.REGISTRATION));
    }

    @Test
    void transitionState_notFound_throwsException() {
        TournamentManager manager = new TournamentManager();
        assertThrows(IllegalArgumentException.class,
                () -> manager.transitionState("NonExistent", TournamentState.STARTING));
    }

    @Test
    void canTransition_allValidPaths_returnTrue() {
        TournamentManager manager = new TournamentManager();
        assertTrue(manager.canTransition(TournamentState.REGISTRATION, TournamentState.STARTING));
        assertTrue(manager.canTransition(TournamentState.STARTING, TournamentState.IN_PROGRESS));
        assertTrue(manager.canTransition(TournamentState.IN_PROGRESS, TournamentState.ENDED));
    }

    @Test
    void canTransition_allInvalidPaths_returnFalse() {
        TournamentManager manager = new TournamentManager();
        assertFalse(manager.canTransition(TournamentState.REGISTRATION, TournamentState.IN_PROGRESS));
        assertFalse(manager.canTransition(TournamentState.REGISTRATION, TournamentState.ENDED));
        assertFalse(manager.canTransition(TournamentState.STARTING, TournamentState.ENDED));
        assertFalse(manager.canTransition(TournamentState.ENDED, TournamentState.REGISTRATION));
        assertFalse(manager.canTransition(TournamentState.ENDED, TournamentState.STARTING));
        assertFalse(manager.canTransition(TournamentState.ENDED, TournamentState.IN_PROGRESS));
    }

    // ── Team Management ─────────────────────────────────────────────

    @Test
    void addTeam_toExistingTournament_succeeds() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team team = new Team("TeamAlpha", 1);
        Tournament updated = manager.addTeam("Cup", team);
        assertEquals(1, updated.teams().size());
        assertEquals("TeamAlpha", updated.teams().get(0).name());
    }

    @Test
    void addTeam_multipleTeams_allAdded() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        manager.addTeam("Cup", new Team("Alpha", 1));
        Tournament updated = manager.addTeam("Cup", new Team("Beta", 2));
        assertEquals(2, updated.teams().size());
    }

    @Test
    void addTeam_afterRegistrationClosed_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        manager.transitionState("Cup", TournamentState.STARTING);
        assertThrows(IllegalStateException.class,
                () -> manager.addTeam("Cup", new Team("LateTeam", 99)));
    }

    @Test
    void addTeam_toNonExistentTournament_throwsException() {
        TournamentManager manager = new TournamentManager();
        assertThrows(IllegalArgumentException.class,
                () -> manager.addTeam("NonExistent", new Team("Team", 1)));
    }

    @Test
    void removeTeam_existingTeam_succeeds() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team team = new Team("TeamAlpha", 1);
        manager.addTeam("Cup", team);
        Tournament updated = manager.removeTeam("Cup", team.id());
        assertEquals(0, updated.teams().size());
    }

    @Test
    void removeTeam_afterRegistrationClosed_throwsException() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team team = new Team("TeamAlpha", 1);
        manager.addTeam("Cup", team);
        manager.transitionState("Cup", TournamentState.STARTING);
        assertThrows(IllegalStateException.class,
                () -> manager.removeTeam("Cup", team.id()));
    }

    @Test
    void getTeam_existingTeam_returnsTeam() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        Team team = new Team("TeamAlpha", 1);
        manager.addTeam("Cup", team);
        Optional<Team> found = manager.getTeam("Cup", team.id());
        assertTrue(found.isPresent());
        assertEquals("TeamAlpha", found.get().name());
    }

    @Test
    void getTeam_notFound_returnsEmpty() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertTrue(manager.getTeam("Cup", UUID.randomUUID()).isEmpty());
    }

    @Test
    void getTeamCount_returnsCorrectCount() {
        TournamentManager manager = new TournamentManager();
        manager.createTournament("Cup", "duels", BracketType.SINGLE_ELIMINATION);
        assertEquals(0, manager.getTeamCount("Cup"));
        manager.addTeam("Cup", new Team("Alpha", 1));
        assertEquals(1, manager.getTeamCount("Cup"));
        manager.addTeam("Cup", new Team("Beta", 2));
        assertEquals(2, manager.getTeamCount("Cup"));
    }

    @Test
    void getTeamCount_nonExistentTournament_returnsZero() {
        TournamentManager manager = new TournamentManager();
        assertEquals(0, manager.getTeamCount("NonExistent"));
    }
}
