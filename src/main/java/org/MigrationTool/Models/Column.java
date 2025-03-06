package org.MigrationTool.Models;

public class Column {
    private String name;
    private String type;
    private Constraints constraints;
    private String newDataType;

    public Column() {}

    @Override
    public String toString() {
        return name + " " + type + " " + constraints;
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

    public Constraints getConstraints() {
        return constraints;
    }
    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    public String getNewDataType() {
        return newDataType;
    }
    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }
}
