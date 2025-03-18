package org.migrationtool.actions;

import org.migrationtool.models.Constraint;
import org.migrationtool.models.ConstraintType;
import org.migrationtool.utils.ChecksumGenerator;
import org.migrationtool.utils.SQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class DropConstraintAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(DropConstraintAction.class);
    private final Constraint constraint;

    public DropConstraintAction(Constraint constraint) {
        this.constraint = constraint;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing DropConstraintAction on table {} with constraint {}", constraint.getTableName(), constraint.getName());
        String query = buildDropConstraintQuery();
        if (query != null) {
            executeSQL(connection, query);
        } else {
            logger.warn("Unsupported constraint type: {} on table {}", constraint.getType(), constraint.getTableName());
        }
    }

    private String buildDropConstraintQuery() {
        if (constraint.isNamed()) {
            return SQLConstants.ALTER_TABLE + constraint.getTableName()
                    + SQLConstants.DROP_CONSTRAINT + constraint.getName()
                    + SQLConstants.SEMICOLON;
        } else if (constraint.getType() == ConstraintType.NOT_NULL) {
            return SQLConstants.ALTER_TABLE + constraint.getTableName()
                    + SQLConstants.ALTER_COLUMN + constraint.getColumnName()
                    + SQLConstants.DROP_NOT_NULL + SQLConstants.SEMICOLON;
        }
        return null;
    }

    @Override
    public String generateChecksum() {
        String string = "DropConstraint: " + constraint.getTableName() + "|" + constraint;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
