package org.MigrationTool.Actions;

import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Database.DatabasePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ModifyColumnTypeAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(ModifyColumnTypeAction.class);
    private final String tableName;
    private final String columnName;
    private final String newDataType;

    public ModifyColumnTypeAction(String tableName, String columnName, String newDataType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.newDataType = newDataType;
    }


    @Override
    public void execute() {
        logger.info("Executing ModifyColumnTypeAction on table: {}, column: {}, new data type: {}", tableName, columnName, newDataType);
        String query = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + newDataType + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Successfully modified type of column: {} to new data type: {}", columnName, newDataType);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing ModifyColumnTypeAction on table: " + tableName, e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("ModifyColumn:" + tableName + "|" + columnName + newDataType);
    }
}
