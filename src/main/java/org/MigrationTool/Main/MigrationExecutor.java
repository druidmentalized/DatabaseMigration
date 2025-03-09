package org.MigrationTool.Main;

import org.MigrationTool.Actions.MigrationAction;
import org.MigrationTool.Parsers.MigrationParser;
import org.MigrationTool.Utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MigrationExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MigrationExecutor.class);

    public void executeMigrations() {
        MigrationParser migrationParser = new MigrationParser();
        List<Migration> migrations = migrationParser.parseMigrations(ConfigLoader.getProperty("migration.file"));
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
            } catch (Exception e) {
                logger.error("Migration ID={} failed: {}", migration.getId(), e.getMessage(), e);
                throw new RuntimeException("Migration execution failed for ID=" + migration.getId(), e);
            }
        }

        logger.info("Migration execution completed.");
    }

    public void rollbackMigrations(int rollbackAmount) {
        MigrationParser migrationParser = new MigrationParser();
        List<Migration> migrations = migrationParser.parseMigrations(ConfigLoader.getProperty("migration.file"));
        MigrationHistory migrationHistory = new MigrationHistory();

        if (migrations == null || migrations.isEmpty()) {
            logger.warn("No migrations found in the XML file.");
            return;
        }

        for (int i = migrations.size() - 1; i >= migrations.size() - rollbackAmount; i--) {
            Migration migration = migrations.get(i);

            if (!migrationHistory.alreadyExecuted(migration)) {
                logger.info("Skipping non executed migration: ID={}, Author={}", migration.getId(), migration.getAuthor());
                continue;
            }

            logger.info("Rolling back migration: ID = {}, Author={}", migration.getId(), migration.getAuthor());

            try {
                for (MigrationAction rollbackAction : migration.getRollbackActions()) {
                    logger.debug("Rolling back action: {}", rollbackAction.getClass().getSimpleName());
                    rollbackAction.execute();
                    logger.info("Successfully rolled back action: {}", rollbackAction.getClass().getSimpleName());
                }

                migrationHistory.deleteRolledBackMigration(migration);
            }
            catch (Exception e) {
                logger.error("Rolling back migration ID={} failed: {}", migration.getId(), e.getMessage(), e);
                throw new RuntimeException("Migration rollback failed for ID=" + migration.getId(), e);
            }

            logger.info("Migration rollback completed.");
        }
    }

}
