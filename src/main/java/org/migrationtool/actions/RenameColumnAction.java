package org.migrationtool.actions;

import org.migrationtool.models.Column;
import org.migrationtool.utils.ChecksumGenerator;
import org.migrationtool.utils.SQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class RenameColumnAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(RenameColumnAction.class);
    private final Column column;

    public RenameColumnAction(Column column) {
        this.column = column;
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing RenameColumnAction on table '{}', to rename column '{}' to '{}'", column.getTableName(), column.getName(), column.getNewName());
        executeSQL(connection, buildRenameColumnQuery());
    }

    private String buildRenameColumnQuery() {
        return SQLConstants.ALTER_TABLE + column.getTableName()
                + SQLConstants.RENAME_COLUMN + column.getName()
                + SQLConstants.TO + column.getNewName()
                + SQLConstants.SEMICOLON;
    }

    @Override
    public String generateChecksum() {
        String string = "RenameColumn: " + column.getTableName() + "|" + column;
        return ChecksumGenerator.generateWithSHA256(string);
    }
}
