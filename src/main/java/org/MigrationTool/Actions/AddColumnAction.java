package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class AddColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(AddColumnAction.class);
    private final Column column;

    public AddColumnAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.info("Executing AddColumnAction on table: {}, column: {}", column.getTableName(), column.getName());

        StringBuilder query = new StringBuilder("ALTER TABLE ")
                .append(column.getTableName())
                .append(" ADD COLUMN ")
                .append(column.getName())
                .append(" ")
                .append(column.getType());

        //appending all unnamed constraints
        for (Constraint constraint : column.getConstraintsList()) {
            if (!constraint.isNamed()) {
                query.append(" ").append(constraint);
            }
        }

        query.append(";");

        try {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query.toString());
            logger.info("Column '{}' added successfully.", column.getName());
        }
        catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing AddColumnAction on table: " + column.getTableName(), e);
        }

        //adding all named constraints
        for (Constraint constraint : column.getConstraintsList()) {
            if (constraint.isNamed()) {
                new AddConstraintAction(constraint).execute(connection);
            }
        }
    }

    @Override
    public String generateChecksum() {
        //creating specific signature
        String string = "AddColumn:" + column.getTableName() + "|" + column;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
