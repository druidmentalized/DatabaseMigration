package org.migrationTool.actions;

import java.sql.Connection;

public interface MigrationAction {
    void execute(Connection connection);
    String generateChecksum();
}
