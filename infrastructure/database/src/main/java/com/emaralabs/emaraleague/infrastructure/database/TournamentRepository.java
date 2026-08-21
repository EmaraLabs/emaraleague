package com.emaralabs.emaraleague.infrastructure.database;

import com.emaralabs.emaraleague.core.tournament.Tournament;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TournamentRepository implements Repository<Tournament, UUID> {

    private final DatabaseManager databaseManager;
    private final ExecutorService executor;

    public TournamentRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public CompletableFuture<Tournament> save(Tournament entity) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO tournaments (id, name, mode, bracket_type, state, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.id().toString());
                stmt.setString(2, entity.name());
                stmt.setString(3, entity.mode());
                stmt.setString(4, entity.bracketType().name());
                stmt.setString(5, entity.state().name());
                stmt.setLong(6, System.currentTimeMillis());
                stmt.setLong(7, System.currentTimeMillis());
                stmt.executeUpdate();
                return entity;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save tournament", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Optional<Tournament>> findById(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM tournaments WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(mapResultSetToTournament(rs));
                }
                return Optional.empty();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find tournament", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<List<Tournament>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM tournaments";
            List<Tournament> tournaments = new ArrayList<>();
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tournaments.add(mapResultSetToTournament(rs));
                }
                return tournaments;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to find all tournaments", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> delete(UUID id) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM tournaments WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete tournament", e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Tournament> update(Tournament entity) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE tournaments SET name = ?, mode = ?, bracket_type = ?, state = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, entity.name());
                stmt.setString(2, entity.mode());
                stmt.setString(3, entity.bracketType().name());
                stmt.setString(4, entity.state().name());
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setString(6, entity.id().toString());
                stmt.executeUpdate();
                return entity;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update tournament", e);
            }
        }, executor);
    }

    private Tournament mapResultSetToTournament(ResultSet rs) throws SQLException {
        return new Tournament(
            UUID.fromString(rs.getString("id")),
            rs.getString("name"),
            rs.getString("mode"),
            com.emaralabs.emaraleague.core.tournament.BracketType.valueOf(rs.getString("bracket_type")),
            com.emaralabs.emaraleague.core.tournament.TournamentState.valueOf(rs.getString("state")),
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    public void shutdown() {
        executor.shutdown();
    }
}
