package org.MigrationTool.Actions;

import org.MigrationTool.Models.Column;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Database.DatabasePool;
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
    public void execute() {
        logger.info("Executing DropColumnAction on table: {}, column: {}", column.getTableName(), column.getName());
        String query = "ALTER TABLE " + column.getTableName() + " DROP COLUMN " + column.getName() + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Successfully dropped column: {}", column.getName());
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
