package org.migrationTool.utils;

import java.util.List;

public class NameGenerator {
    private NameGenerator() {}

    public static String generateConstraintName(String tableName, String columnName, String constraintType) {
        return constraintType.toLowerCase() +
                "_" + tableName +
                "_" + columnName;
    }

    public static String generateIndexName(String tableName, List<String> columnNames) {
        return tableName + "_" + String.join("_", columnNames);
    }
}
