package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class RenameTableAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(RenameTableAction.class);
    private final String tableName;
    private final String newTableName;

    public RenameTableAction(String tableName, String newTableName) {
        this.tableName = tableName;
        this.newTableName = newTableName;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Renaming table {} to {}", tableName, newTableName);
        String query = "ALTER TABLE " + tableName + " RENAME TO " + newTableName;

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Table {} successfully renamed", newTableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing RenameTableAction: " + e.getMessage());
        }
    }

    @Override
    public String generateChecksum() {
        String string = "DropConstraint: " + tableName + "|" + newTableName;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
