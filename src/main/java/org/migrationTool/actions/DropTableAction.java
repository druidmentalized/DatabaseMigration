package org.migrationTool.actions;

import org.migrationTool.utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropTableAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(DropTableAction.class);
    private final String tableName;

    public DropTableAction(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("      Executing DropTableAction on table: {}", tableName);
        String query = "DROP TABLE " + tableName + ";";

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Dropped table: {}", tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropTableAction on table: " + tableName, e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("DropTable:" + tableName);
    }
}
