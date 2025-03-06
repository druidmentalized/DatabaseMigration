package org.MigrationTool.Models;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String toString() {
        List<String> constraintList = new ArrayList<>();

        if (primaryKey) constraintList.add("PRIMARY KEY");
        if (autoIncrement) constraintList.add("AUTO_INCREMENT");
        if (!nullable) constraintList.add("NOT NULL");
        if (unique) constraintList.add("UNIQUE");

        return String.join(" ", constraintList);
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
