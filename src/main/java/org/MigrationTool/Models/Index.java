package org.MigrationTool.Models;

import java.util.ArrayList;
import java.util.List;

public class Index {
    private String name;
    private final List<String> columns = new ArrayList<>();
    private boolean unique;

    public Index() {}

    @Override
    public String toString() {
        return (unique ? "UNIQUE " : "") + "INDEX on columns (" + String.join(", ", columns) + ")";
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumns() {
        return columns;
    }

    public boolean isUnique() {
        return unique;
    }
    public void setUnique(boolean unique) {
        this.unique = unique;
    }
}
