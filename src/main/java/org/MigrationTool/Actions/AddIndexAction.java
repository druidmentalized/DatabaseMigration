package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Index;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class AddIndexAction implements MigrationAction {
    private final static Logger logger = LoggerFactory.getLogger(AddIndexAction.class);
    private final Index index;

    public AddIndexAction(Index index) {
        this.index = index;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing AddIndexAction on table {}", index.getTableName());
        String query = "CREATE " + (index.isUnique() ? "UNIQUE " : "") + "INDEX " + index.getName()
                + " ON " + index.getTableName() + " (" + String.join(", ", index.getColumns()) + ");";

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Added Index {} to table {}", index.getName(), index.getTableName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing CreateIndexAction: " + e.getMessage(), e);
        }

    }

    @Override
    public String generateChecksum() {
        String string = "AddIndex: " + index.getTableName() + "|" + index;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
