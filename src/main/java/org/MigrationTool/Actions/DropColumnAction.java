package org.MigrationTool.Actions;

import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Utils.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(DropColumnAction.class);
    private final String tableName;
    private final String columnName;

    public DropColumnAction(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    @Override
    public void execute() {
        logger.info("Executing DropColumnAction on table: {}, column: {}", tableName, columnName);
        String query = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Successfully dropped column: {}", columnName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropColumnAction on table: " + tableName + ", column: " + columnName, e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("DropColumn:" + tableName + "|" + columnName);
    }
}
