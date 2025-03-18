package org.migrationtool.actions;

import org.migrationtool.models.Column;
import org.migrationtool.models.Constraint;
import org.migrationtool.utils.ChecksumGenerator;
import org.migrationtool.utils.SQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class AddColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(AddColumnAction.class);
    private final Column column;

    public AddColumnAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing AddColumnAction on table: {}, column: {}", column.getTableName(), column.getName());

        executeSQL(connection, buildAddColumnQuery());
        addNamedConstraints(connection);
    }

    private String buildAddColumnQuery() {
        return SQLConstants.ALTER_TABLE + column.getTableName() +
                SQLConstants.ADD_COLUMN + column.getName() +
                SQLConstants.SPACE + column.getType() +
                SQLConstants.SEMICOLON;
    }

    private void addNamedConstraints(Connection connection) {
        for (Constraint constraint : column.getConstraintsList()) {
            if (constraint.isNamed()) {
                new AddConstraintAction(constraint).execute(connection);
            }
        }
    }

    @Override
    public String generateChecksum() {
        return ChecksumGenerator.generateWithSHA256("AddColumn:" + column.getTableName() + "|" + column);
    }
}
