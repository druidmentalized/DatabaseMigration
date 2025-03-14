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
    private final Constraint constraint;

    public AddConstraintAction(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing AddConstraint on table {} with constraint {}", constraint.getTableName(), constraint);

        String query;

        if (constraint.isNamed()) {
            query = String.format("ALTER TABLE %s ADD CONSTRAINT %s;", constraint.getTableName(), constraint);
        }
        else {
            query = String.format("ALTER TABLE %s ALTER COLUMN %s SET %s;", constraint.getTableName(), constraint.getColumnName(), constraint);
        }

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Constraint '{}' added", constraint.getName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing AddConstraintAction: " + e.getMessage());
        }
    }

    @Override
    public String generateChecksum() {
        //creating special signature
        String string = "AddConstraint:" + constraint.getTableName() + "|" + constraint;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
