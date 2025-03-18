package org.migrationtool.actions;

import org.migrationtool.utils.ChecksumGenerator;
import org.migrationtool.utils.SQLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

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
        executeSQL(connection, buildRenameTableQuery());
    }

    private String buildRenameTableQuery() {
        return SQLConstants.ALTER_TABLE + tableName
                + SQLConstants.RENAME_TO + newTableName
                + SQLConstants.SEMICOLON;
    }

    @Override
    public String generateChecksum() {
        String string = "RenameTable:" + tableName + "|" + newTableName;
        return ChecksumGenerator.generateWithSHA256(string);
    }

}
