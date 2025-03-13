package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class RenameColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(RenameColumnAction.class);
    private final Column column;

    public RenameColumnAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.info("Executing RenameColumnAction on table '{}', to rename column '{}' to '{}'", column.getTableName(), column.getName(), column.getNewName());
        String query = "ALTER TABLE " + column.getTableName() + " RENAME COLUMN " + column.getName() + " TO " + column.getNewName();

        try {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Renamed column '{}' to '{}'", column.getName(), column.getNewName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing RenameColumnAction" + e.getMessage(), e);
        }

    }

    @Override
    public String generateChecksum() {
        String string = "RenameColumn: " + column.getTableName() + "|" + column;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
