package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class AddConstraintAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(AddConstraintAction.class);
    private final String tableName;
    private final Constraint constraint;

    public AddConstraintAction(String tableName, Constraint constraint) {
        this.tableName = tableName;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        logger.info("Executing AddConstraint on table {} with constraint {}", tableName, constraint);

        String query;

        if (constraint.isNamed()) {
            query = String.format("ALTER TABLE %s ADD %s;", tableName, constraint);
        }
        else {
            query = String.format("ALTER TABLE %s ALTER COLUMN %s SET %s;", tableName, constraint.getColumnName(), constraint);
        }

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Constraint '{}' successfully added", constraint);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing AddConstraintAction: " + e.getMessage());
        }
    }

    @Override
    public String generateChecksum() {
        //creating special signature
        String stringBuilder = "AddNamedConstraint:" + tableName + "|" + constraint;

        return ChecksumGenerator.generateWithSHA256(stringBuilder);
    }
}
