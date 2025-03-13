package org.MigrationTool.Models;

import java.util.ArrayList;
import java.util.List;

public class Column {
    private String name;
    private String type;
    private String newDataType;
    private String newColumnName;
    private final List<Constraint> constraintsList = new ArrayList<>();

    public Column() {}

    @Override
    public String toString() {
        return name + " " + type;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public List<Constraint> getConstraintsList() {
        return constraintsList;
    }

    public String getNewDataType() {
        return newDataType;
    }
    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }

    public String getNewColumnName() {
        return newColumnName;
    }
    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }
}
