package org.MigrationTool.Models;

public class Constraint {
    private String name;
    private String expression;
    private ConstraintType type;

    public Constraint() {}

    @Override
    public String toString() {
        return switch (type) {
            case AUTO_INCREMENT -> "AUTO_INCREMENT";
            case NOT_NULL -> "NOT NULL";
            case CHECK -> String.format("CONSTRAINT %s CHECK (%s)", name, expression);
            case FOREIGN_KEY -> String.format("CONSTRAINT %s FOREIGN KEY (%s)", name, expression);
            case UNIQUE -> String.format("CONSTRAINT %s UNIQUE (%s)", name, expression);
            case PRIMARY_KEY -> String.format("CONSTRAINT %s PRIMARY KEY (%s)", name, expression);
        };
    }

    public boolean isNamed() {
        return (type != ConstraintType.AUTO_INCREMENT) && (type != ConstraintType.NOT_NULL);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }

    public ConstraintType getType() {
        return type;
    }
    public void setType(ConstraintType type) {
        this.type = type;
    }
}
