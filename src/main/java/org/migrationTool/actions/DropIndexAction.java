package org.migrationTool.actions;

import org.migrationTool.models.Index;
import org.migrationTool.utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropIndexAction implements MigrationAction {
    private final static Logger logger = LoggerFactory.getLogger(DropIndexAction.class);
    private final Index index;

    public DropIndexAction(Index index) {
        this.index = index;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing DropIndexAction on table {} with index {}", index.getTableName(), index.getName());
        String query = "DROP INDEX " + index.getName();

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Dropped Index {} from table {}", index.getName(), index.getTableName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropIndexAction: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateChecksum() {
        String string = "DropIndex: " + index.getTableName() + "|" + index;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
