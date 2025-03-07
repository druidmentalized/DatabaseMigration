package org.MigrationTool.Main;

import org.MigrationTool.Utils.ConfigLoader;
import org.MigrationTool.Database.DatabasePool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MigrationHistory {

    public boolean alreadyExecuted(Migration migration) {
        String query = "SELECT COUNT(*) FROM Migration_Table WHERE Checksum = '" + migration.getChecksum() + "'";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Checking migration failed: " + e.getMessage(), e);
        }

        return false;
    }

    public void storeSuccessfulMigration(Migration migration) {
        StringBuilder query = new StringBuilder("INSERT INTO Migration_Table (author, filename, checksum) VALUES ('");
        query.append(migration.getAuthor()).append("', '");
        query.append(ConfigLoader.getProperty("migration.folder")).append("', '");
        query.append(migration.getChecksum()).append("')");

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            connection.createStatement().execute(query.toString());
        } catch (SQLException e) {
            throw new RuntimeException("Storing migration in history failed: " + e.getMessage(), e);
        }
    }
}
