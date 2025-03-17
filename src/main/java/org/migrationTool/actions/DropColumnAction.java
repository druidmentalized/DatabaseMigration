package org.migrationTool.actions;

import org.migrationTool.models.Column;
import org.migrationTool.utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(DropColumnAction.class);
    private final Column column;

    public DropColumnAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing DropColumnAction on table: {}, column: {}", column.getTableName(), column.getName());
        String query = "ALTER TABLE " + column.getTableName() + " DROP COLUMN " + column.getName() + ";";

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Dropped column: {}", column.getName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropColumnAction on table: " + column.getTableName() + ", column: " + column.getName(), e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("DropColumn:" + column.getTableName() + "|" + column);
    }
}
