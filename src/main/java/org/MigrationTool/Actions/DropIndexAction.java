package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Index;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropIndexAction implements MigrationAction {
    private final static Logger logger = LoggerFactory.getLogger(DropIndexAction.class);
    private final String tableName;
    private final Index index;

    public DropIndexAction(String tableName, Index index) {
        this.tableName = tableName;
        this.index = index;
    }

    @Override
    public void execute() {
        logger.info("Executing DropIndexAction on table {} with index {}", tableName, index.getName());
        String query = "DROP INDEX " + index.getName();

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Successfully dropped Index {} from table {}", index.getName(), tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropIndexAction: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateChecksum() {
        String string = "DropIndex: " + tableName + "|" + index;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
