package org.MigrationTool.Utils;

public class AttributeNames {
    private AttributeNames() {}

    //migration
    public static String id = "id";
    public static String author = "author";

    //actions
    public static String tableName = "tableName";
    public static String newTableName = "newTableName";

    //columns
    public static String columnName = "columnName";
    public static String columnType = "columnType";
    public static String newDataType = "newDataType";
    public static String newColumnName = "newColumnName";

    //constraints
    public static String constraintType = "type";
    public static String constraintName = "constraintName";
    public static String expression = "expression";

    //indices
    public static String indexUniqueness = "uniqueness";
}
