package org.MigrationTool.Main;

import org.MigrationTool.Actions.MigrationAction;
import org.MigrationTool.Utils.ChecksumGenerator;

import java.util.List;

public class Migration {
    private final int id;
    private final String author;
    private final String checksum;
    private final List<MigrationAction> migrationActions;
    private final List<MigrationAction> rollbackActions;

    public Migration(int id, String author, List<MigrationAction> migrationActions, List<MigrationAction> rollbackActions) {
        this.id = id;
        this.author = author;
        this.migrationActions = migrationActions;
        this.rollbackActions = rollbackActions;

        checksum = generateChecksum();
    }

    private String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder("OperationId:").append(id)
                .append("|Author:").append(author).append("|");
        for (MigrationAction action : migrationActions) {
            stringBuilder.append(action.generateChecksum());
        }
        stringBuilder.append("|");
        for (MigrationAction action : rollbackActions) {
            stringBuilder.append(action.generateChecksum());
        }

        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }

    public List<MigrationAction> getMigrationActions() {
        return migrationActions;
    }

    public List<MigrationAction> getRollbackActions() {
        return rollbackActions;
    }

    public String getChecksum() {
        return checksum;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }
}
