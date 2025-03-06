package org.MigrationTool.Main;

import org.MigrationTool.Utils.MigrationTableInitializer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting MigrationTool");
        //todo: trying to load migration table(if wasn't made before)
        MigrationTableInitializer.initialize();
        new MigrationExecutor().executeMigrations();
    }
}