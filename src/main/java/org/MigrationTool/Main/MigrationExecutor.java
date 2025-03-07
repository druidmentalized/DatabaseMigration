package org.MigrationTool.Main;

import org.MigrationTool.Actions.MigrationAction;
import org.MigrationTool.Parsers.MigrationParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MigrationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MigrationExecutor.class);

    public void executeMigrations() {
        MigrationParser migrationParser = new MigrationParser();
        List<Migration> migrations = migrationParser.parseMigrations("src/main/resources/migrations.xml");
        MigrationHistory migrationHistory = new MigrationHistory();

        if (migrations == null || migrations.isEmpty()) {
            logger.warn("No migrations found in the XML file.");
            return;
        }

        for (Migration migration : migrations) {
            if (migrationHistory.alreadyExecuted(migration)) {
                logger.info("Skipping already executed migration: ID={}, Author={}", migration.getId(), migration.getAuthor());
                continue;
            }

            logger.info("Executing migration: ID={}, Author={}", migration.getId(), migration.getAuthor());

            try {
                for (MigrationAction migrationAction : migration.getMigrationActions()) {
                    logger.debug("Executing action: {}", migrationAction.getClass().getSimpleName());
                    migrationAction.execute();
                    logger.info("Successfully executed action: {}", migrationAction.getClass().getSimpleName());
                }

                migrationHistory.storeSuccessfulMigration(migration);
                logger.info("Migration ID={} executed successfully and stored.", migration.getId());

            } catch (Exception e) {
                logger.error("Migration ID={} failed: {}", migration.getId(), e.getMessage(), e);
                throw new RuntimeException("Migration execution failed for ID=" + migration.getId(), e);
            }
        }

        logger.info("Migration execution completed.");
    }
}
