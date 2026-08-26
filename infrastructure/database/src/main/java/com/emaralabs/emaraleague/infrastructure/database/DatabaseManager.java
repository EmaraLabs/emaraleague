package com.emaralabs.emaraleague.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initializeSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tournaments (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    mode VARCHAR(100) NOT NULL,
                    bracket_type VARCHAR(50) NOT NULL,
                    state VARCHAR(50) NOT NULL,
                    format VARCHAR(20) DEFAULT 'TEAM',
                    team_size INT DEFAULT 2,
                    started_at BIGINT DEFAULT 0,
                    ended_at BIGINT DEFAULT 0,
                    created_at BIGINT NOT NULL,
                    updated_at BIGINT NOT NULL
                )
                """);
            
            // Migration: add new columns if they don't exist (for existing databases)
            migrateTournamentTable(conn);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    id VARCHAR(36) PRIMARY KEY,
                    tournament_id VARCHAR(36) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    seed INT NOT NULL,
                    created_at BIGINT NOT NULL
                )
                """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS matches (
                    id VARCHAR(36) PRIMARY KEY,
                    tournament_id VARCHAR(36) NOT NULL,
                    team_a_id VARCHAR(36) NOT NULL,
                    team_b_id VARCHAR(36) NOT NULL,
                    state VARCHAR(50) NOT NULL,
                    winner_id VARCHAR(36),
                    scheduled_at BIGINT NOT NULL,
                    completed_at BIGINT
                )
                """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    team_id VARCHAR(36),
                    wins INT DEFAULT 0,
                    losses INT DEFAULT 0,
                    kills INT DEFAULT 0,
                    deaths INT DEFAULT 0,
                    created_at BIGINT NOT NULL,
                    updated_at BIGINT NOT NULL
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    /**
     * Migrate existing tournament table to add new columns.
     * SQLite doesn't support IF NOT EXISTS for ALTER TABLE, so we check manually.
     */
    private void migrateTournamentTable(Connection conn) throws SQLException {
        String[] newColumns = {
            "ALTER TABLE tournaments ADD COLUMN format VARCHAR(20) DEFAULT 'TEAM'",
            "ALTER TABLE tournaments ADD COLUMN team_size INT DEFAULT 2",
            "ALTER TABLE tournaments ADD COLUMN started_at BIGINT DEFAULT 0",
            "ALTER TABLE tournaments ADD COLUMN ended_at BIGINT DEFAULT 0"
        };

        for (String sql : newColumns) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                // Column already exists — ignore
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
