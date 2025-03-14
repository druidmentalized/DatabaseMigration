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


            //todo: split these two parts
            logger.info("===========================================");
            logger.info("     STARTING MIGRATION EXECUTION");
            logger.info("===========================================");
            logger.info("");
            new MigrationExecutor().executeMigrations();

/*            logger.info("===========================================");
            logger.info("     STARTING ROLLBACK EXECUTION");
            logger.info("===========================================");
            logger.info("");
            new MigrationExecutor().rollbackMigrations(0);*/


            logger.info("");
            logger.info("===========================================");
            logger.info("    EXECUTION COMPLETED SUCCESSFULLY");
            logger.info("===========================================");
        } catch (Exception e) {
            logger.error("MigrationTool encountered a fatal error: {}", e.getMessage(), e);
            System.exit(1);
        }

        logger.info("MigrationTool finished execution.");
    }
}