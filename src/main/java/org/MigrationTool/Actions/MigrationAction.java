package org.MigrationTool.Actions;

import java.sql.Connection;

public interface MigrationAction {
    void execute(Connection connection);
    String generateChecksum();
}
