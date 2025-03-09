package org.MigrationTool.Main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting MigrationTool...");

        try {
            logger.info("Initializing migration history table...");
            MigrationTableInitializer.initialize();
            logger.info("Migration history table initialized successfully.");

            logger.info("Executing migrations...");
            new MigrationExecutor().executeMigrations();
            //new MigrationExecutor().rollbackMigrations(3);
            logger.info("Migration execution completed successfully.");
        } catch (Exception e) {
            logger.error("MigrationTool encountered a fatal error: {}", e.getMessage(), e);
            System.exit(1);
        }

        logger.info("MigrationTool finished execution.");
    }
}