package org.MigrationTool.Main;

import org.MigrationTool.Utils.MigrationTableInitializer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting MigrationTool");
        MigrationTableInitializer.initialize();
        new MigrationExecutor().executeMigrations();
    }
}