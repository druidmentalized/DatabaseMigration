package org.MigrationTool.Utils;

public class ConstraintNameGenerator {
    public static String generateConstraintName(String tableName, String columnName, String constraintType, int migrationId) {
        return constraintType.toLowerCase() +
                "_" + tableName +
                "_" + columnName +
                "_" + String.format("%03d", migrationId);
    }
}
