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
    private final String tableName;
    private final Column column;

    public AddColumnAction(String tableName, Column column) {
        this.tableName = tableName;
        this.column = column;
    }

    @Override
    public void execute() {
        logger.info("Executing AddColumnAction on table: {}, column: {}", tableName, column.getName());

        StringBuilder query = new StringBuilder("ALTER TABLE ")
                .append(tableName)
                .append(" ADD COLUMN ")
                .append(column);

        //appending all unnamed constraints
        for (Constraint constraint : column.getConstraintsList()) {
            if (!constraint.isNamed()) {
                query.append(" ").append(constraint);
            }
        }

        query.append(";");

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query.toString());
            logger.info("Column '{}' added successfully.", column.getName());
        }
        catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing AddColumnAction on table: " + tableName, e);
        }

        //adding all named constraints
        for (Constraint constraint : column.getConstraintsList()) {
            if (constraint.isNamed()) {
                new AddConstraintAction(tableName, constraint).execute();
            }
        }
    }

    @Override
    public String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder();

        //creating specific signature
        stringBuilder.append("AddColumn:").append(tableName).append("|");
        stringBuilder.append(column);
        column.getConstraintsList().forEach(stringBuilder::append);

        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }
}
