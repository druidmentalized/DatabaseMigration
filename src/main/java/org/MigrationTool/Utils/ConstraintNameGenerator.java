package org.MigrationTool.Utils;

public class ConstraintNameGenerator {
    public static String generateConstraintName(String tableName, String columnName, String constraintType) {
        return constraintType.toLowerCase() +
                "_" + tableName +
                "_" + columnName;
    }
}
