package org.MigrationTool.Models;

public class Constraints {
    private boolean primaryKey;
    private boolean autoIncrement;
    private boolean nullable;
    private boolean unique;

    public Constraints() {
        this.primaryKey = false;
        this.autoIncrement = false;
        this.nullable = false;
        this.unique = false;
    }

    public Constraints(boolean primaryKey, boolean autoIncrement, boolean nullable, boolean unique) {
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.nullable = nullable;
        this.unique = unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isNullable() {
        return nullable;
    }
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isUnique() {
        return unique;
    }
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
