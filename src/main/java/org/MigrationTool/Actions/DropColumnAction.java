package org.MigrationTool.Actions;

import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Utils.DatabasePool;

import java.sql.Connection;
import java.sql.SQLException;

public class DropColumnAction implements MigrationAction {
    private final String tableName;
    private final String columnName;

    public DropColumnAction(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    @Override
    public void execute() {
        String query = "ALTER TABLE " + tableName + " DROP COLUMN " + columnName + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Migration execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("DropColumn:" + tableName + "|" + columnName);
    }
}
