package org.MigrationTool.Main;

import org.MigrationTool.Actions.MigrationAction;
import org.MigrationTool.Parsers.MigrationParser;

import java.util.List;

public class MigrationExecutor {
    public void executeMigrations() {
        MigrationParser migrationParser = new MigrationParser();
        List<Migration> migrations = migrationParser.parseMigrations("src/main/resources/migrations.xml");

        for (Migration migration : migrations) {
            if (!alreadyExecuted(migration)) {
                for (MigrationAction migrationAction : migration.getMigrationActions()) {
                    migrationAction.execute();
                }
                storeSuccessfulMigration(migration);
            }
            //todo: add logging
        }

        //todo: closing everything
    }

    private boolean alreadyExecuted(Migration migration) {
        return false;
    }

    private void storeSuccessfulMigration(Migration migration) {

    }
}
