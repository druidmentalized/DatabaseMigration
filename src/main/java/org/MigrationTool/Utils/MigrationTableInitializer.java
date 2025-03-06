package org.MigrationTool.Utils;

import java.sql.Connection;
import java.sql.SQLException;

public class MigrationTableInitializer {
    public static void initialize() {
        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            String query = "CREATE TABLE IF NOT EXISTS migration_table (" +
                    "id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                    "author VARCHAR(255) NOT NULL, " +
                    "filename VARCHAR(255) NOT NULL, " +
                    "executionDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "checksum VARCHAR(255) NOT NULL);";

            connection.createStatement().execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
