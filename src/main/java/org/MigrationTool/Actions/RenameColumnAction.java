package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class RenameColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(RenameColumnAction.class);
    private final String tableName;
    private final String columnName;
    private final String newColumnName;

    public RenameColumnAction(String tableName, String columnName, String newColumnName) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.newColumnName = newColumnName;
    }

    @Override
    public void execute() {
        logger.info("Executing RenameColumnAction on table '{}', to rename column '{}' to '{}'", tableName, columnName, newColumnName);
        String query = "ALTER TABLE " + tableName + " RENAME COLUMN " + columnName + " TO " + newColumnName;

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Renamed column '{}' to '{}'", columnName, newColumnName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing RenameColumnAction" + e.getMessage(), e);
        }

    }

    @Override
    public String generateChecksum() {
        String string = "RenameColumn: " + tableName + "|" + columnName + "," + newColumnName;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
