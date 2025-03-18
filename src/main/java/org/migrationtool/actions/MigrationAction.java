package org.migrationtool.actions;

import org.migrationtool.utils.LoggerHelper;

import java.sql.Connection;
import java.sql.SQLException;

public interface MigrationAction {
    void execute(Connection connection);
    String generateChecksum();

    default void executeSQL(Connection connection, String query) {
        try {
            LoggerHelper.logSQLQuery(query);
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            LoggerHelper.logSQLQuery(query);
            throw new RuntimeException("SQL execution failed: " + query, e);
        }
    }
}
