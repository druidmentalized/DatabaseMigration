package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Models.ConstraintType;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DropConstraintAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(DropConstraintAction.class);
    private final Constraint constraint;

    public DropConstraintAction(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing DropConstraintAction on table {} with constarint {}", constraint.getTableName(), constraint.getName());
        String query = "";

        if (constraint.isNamed()) {
            query = "ALTER TABLE " + constraint.getTableName() + " DROP CONSTRAINT " + constraint.getName();
        }
        else if (constraint.getType() == ConstraintType.NOT_NULL) {
            query = "ALTER TABLE " + constraint.getTableName() + " ALTER COLUMN " + constraint.getColumnName() + " DROP NOT NULL";
        }
        else if (constraint.getType() == ConstraintType.AUTO_INCREMENT) {
            //todo: make
        }
        else {
            logger.warn("Unknown constraint type: {}. Skipping...", constraint.getType());
            return;
        }

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query);
            logger.info("Dropped constraint: {}", constraint.getName());
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing DropConstraintAction on table " + constraint.getTableName() + ", constraint " + constraint);
        }
    }

    @Override
    public String generateChecksum() {
        String string = "DropConstraint: " + constraint.getTableName() + "|" + constraint;
        return ChecksumGenerator.generateWithSHA256(string);
    }

    //todo: make helper
    private String getColumnType(String tableName, String columnName) {
       return "";
    }
}
