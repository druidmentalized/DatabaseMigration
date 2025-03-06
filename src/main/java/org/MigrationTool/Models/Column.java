package org.MigrationTool.Models;

public class Column {
    private String name;
    private String type;
    private Constraints constraints;

    public Column(String name, String type, Constraints constraints) {
        this.name = name;
        this.type = type;
        this.constraints = constraints;
    }

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
}
