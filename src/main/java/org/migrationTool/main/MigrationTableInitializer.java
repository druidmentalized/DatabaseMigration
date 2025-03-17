package org.migrationTool.main;

import org.migrationTool.database.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class MigrationTableInitializer {
    private static final Logger logger = LoggerFactory.getLogger(MigrationTableInitializer.class);

    public static void initialize() {
        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            String query = "CREATE TABLE IF NOT EXISTS migration_table (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "migrationID INTEGER NOT NULL, " +
                    "author VARCHAR(255) NOT NULL, " +
                    "filename VARCHAR(255) NOT NULL, " +
                    "executionDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "checksum VARCHAR(255) NOT NULL);";

            connection.createStatement().execute(query);
            logger.info("Migration history table is ready.");
        } catch (SQLException e) {
            logger.error("Failed to initialize migration history table: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing migration history table", e);
        }
    }
}
