package org.MigrationTool.Actions;

import org.MigrationTool.Utils.ChecksumGenerator;
import org.MigrationTool.Utils.DatabasePool;

import java.sql.Connection;
import java.sql.SQLException;

public class ModifyColumnTypeAction implements MigrationAction {
    private final String tableName;
    private final String columnName;
    private final String newDateType;

    public ModifyColumnTypeAction(String tableName, String columnName, String newDateType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.newDateType = newDateType;
    }


    @Override
    public void execute() {
        String query = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " " + newDateType + ";";

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("Migration execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateChecksum() {
        //making specific signature
        return ChecksumGenerator.generateWithSHA256("ModifyColumn:" + tableName + "|" + columnName + newDateType);
    }
}
