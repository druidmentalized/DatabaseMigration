package org.MigrationTool.Main;

import org.MigrationTool.Utils.ConfigLoader;
import org.MigrationTool.Database.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrationHistory {
    private static final Logger logger = LoggerFactory.getLogger(MigrationHistory.class);

    public boolean alreadyExecuted(Migration migration) {
        String query = "SELECT COUNT(*) FROM Migration_Table WHERE Checksum = '" + migration.getChecksum() + "'";

        logger.debug("Checking if migration ID={} has already been executed.", migration.getId());

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(query);

            if (resultSet.next()) {
                boolean result =  resultSet.getInt(1) > 0;
                logger.debug("Migration ID={} execution status: {}", migration.getId(), result ? "ALREADY EXECUTED" : "NOT EXECUTED");
                return result;
            }

        } catch (SQLException e) {
            logger.error("Failed to check if migration history for ID={}: {}", migration.getId(), e.getMessage());
            throw new RuntimeException("Checking migration failed: " + e.getMessage(), e);
        }

        return false;
    }

    public void storeSuccessfulMigration(Migration migration) {
        StringBuilder query = new StringBuilder("INSERT INTO Migration_Table (migrationID, author, filename, checksum) VALUES ('");
        query.append(migration.getId()).append("', '");
        query.append(migration.getAuthor()).append("', '");
        query.append(ConfigLoader.getProperty("migration.file")).append("', '");
        query.append(migration.getChecksum()).append("')");

        logger.info("Storing successful migration ID={}, Author={}", migration.getId(), migration.getAuthor());

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            connection.createStatement().execute(query.toString());

            logger.info("Migration ID={} stored successfully.", migration.getId());
        } catch (SQLException e) {
            logger.error("Failed to store migration ID={}: {}", migration.getId(), e.getMessage());
            throw new RuntimeException("Storing migration in history failed: " + e.getMessage(), e);
        }
    }

    public void deleteRolledBackMigration(Migration migration) {
        String query = "DELETE FROM Migration_Table WHERE Checksum = '" + migration.getChecksum() + "';";

        logger.info("Rolling back migration ID={}", migration.getId());

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            connection.createStatement().execute(query);

            logger.info("Migration ID={} removed from history.", migration.getId());
        } catch (SQLException e) {
            logger.error("Failed to delete migration rolled-back migration ID={}: {}", migration.getId(), e.getMessage());
            throw new RuntimeException("Deleting migration from history failed: " + e.getMessage(), e);
        }
    }
}
