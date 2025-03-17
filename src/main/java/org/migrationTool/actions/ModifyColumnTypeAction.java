package org.migrationTool.actions;

import org.migrationTool.models.Column;
import org.migrationTool.utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ModifyColumnTypeAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(ModifyColumnTypeAction.class);
    private final Column column;

    public ModifyColumnTypeAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing ModifyColumnTypeAction on table: {}, column: {}, new data type: {}", column.getTableName(), column.getName(), column.getNewDataType());
        String query = "ALTER TABLE " + column.getTableName() + " ALTER COLUMN " + column.getName() + " " + column.getNewDataType() + ";";

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Modified type of column: {} to new data type: {}", column.getName(), column.getNewDataType());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing ModifyColumnTypeAction on table: " + column.getTableName(), e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("ModifyColumn:" + column.getTableName() + "|" + column);
    }
}
