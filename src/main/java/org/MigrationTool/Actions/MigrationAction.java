package org.MigrationTool.Actions;

public interface MigrationAction {
    void execute();
    String generateChecksum();
}
