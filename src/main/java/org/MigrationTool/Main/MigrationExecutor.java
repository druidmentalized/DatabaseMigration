package org.MigrationTool.Main;

import org.MigrationTool.Actions.MigrationAction;
import org.MigrationTool.Parsers.MigrationParser;

import java.util.List;

public class MigrationExecutor {
    public void executeMigrations() {
        MigrationParser migrationParser = new MigrationParser();
        List<Migration> migrations = migrationParser.parseMigrations("src/main/resources/migrations.xml");
        MigrationHistory migrationHistory = new MigrationHistory();

        for (Migration migration : migrations) {
            if (!migrationHistory.alreadyExecuted(migration)) {
                for (MigrationAction migrationAction : migration.getMigrationActions()) {
                    migrationAction.execute();
                }
                migrationHistory.storeSuccessfulMigration(migration);
            }
            //todo: add logging
        }

        //todo: closing everything
    }
}
