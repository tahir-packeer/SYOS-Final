package org.syos.test.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.syos.infrastructure.persistence.DBConnection;
import org.syos.application.service.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Base class for all integration tests that need a database.
 * Sets up H2 in-memory database for testing.
 */
public abstract class DatabaseTestBase {

    protected DBConnection dbConnection;
    protected TestLogger logger;

    @BeforeEach
    void setUpDatabase() throws Exception {
        // Initialize test database connection
        dbConnection = DBConnection.getInstance();

        // Load and execute test schema
        String schema = loadResource("/test-schema.sql");
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Split and execute each statement
            String[] statements = schema.split(";");
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    stmt.execute(trimmed);
                }
            }
        }

        // Initialize test logger
        logger = new TestLogger();
    }

    @AfterEach
    void cleanUpDatabase() throws Exception {
        // Clean up database after each test
        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Drop all tables in reverse order to handle foreign keys
            String[] dropStatements = {
                    "DROP TABLE IF EXISTS bill_items",
                    "DROP TABLE IF EXISTS bills",
                    "DROP TABLE IF EXISTS website_inventory",
                    "DROP TABLE IF EXISTS shelf_stock",
                    "DROP TABLE IF EXISTS stock_batch",
                    "DROP TABLE IF EXISTS item",
                    "DROP TABLE IF EXISTS online_customer",
                    "DROP TABLE IF EXISTS customers",
                    "DROP TABLE IF EXISTS users"
            };

            for (String dropSql : dropStatements) {
                stmt.execute(dropSql);
            }
        }
    }

    private String loadResource(String resourcePath) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getResourceAsStream(resourcePath))))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource: " + resourcePath, e);
        }
    }

    /**
     * Test implementation of Logger interface.
     */
    public static class TestLogger implements Logger {
        private final java.util.List<String> logs = new java.util.ArrayList<>();

        @Override
        public void info(String message) {
            logs.add("INFO: " + message);
        }

        @Override
        public void error(String message) {
            logs.add("ERROR: " + message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            logs.add("ERROR: " + message + " - " + throwable.getMessage());
        }

        @Override
        public void debug(String message) {
            logs.add("DEBUG: " + message);
        }

        @Override
        public void warn(String message) {
            logs.add("WARN: " + message);
        }

        public java.util.List<String> getLogs() {
            return new java.util.ArrayList<>(logs);
        }

        public void clear() {
            logs.clear();
        }

        public boolean hasErrorLogs() {
            return logs.stream().anyMatch(log -> log.startsWith("ERROR:"));
        }
    }
}