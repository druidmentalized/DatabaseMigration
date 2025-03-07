package org.MigrationTool.Actions;

import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Database.DatabasePool;
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
    public void execute() {
        logger.info("Executing DropTableAction on table: {}", tableName);
        String query = "DROP TABLE " + tableName + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Successfully dropped table: {}", tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropTableAction on table: " + tableName, e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("DropTable:" + tableName + "|");
    }
}
