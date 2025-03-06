package org.MigrationTool.Actions;

import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
import org.MigrationTool.Utils.ChecksumGenerator;

public class AddColumnAction implements MigrationAction {
    private String tableName;
    private Column column;

    public AddColumnAction(String tableName, Column column) {
        this.tableName = tableName;
        this.column = column;
    }

    @Override
    public void execute() {

    }

    @Override
    public String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder();

        //creating specific signature
        stringBuilder.append("AddColumn:").append(tableName).append("|");
        stringBuilder.append(column.getName()).append(column.getType());
        Constraints constraints = column.getConstraints();
        if (constraints != null) {
            stringBuilder.append("PrimaryKey=").append(constraints.isPrimaryKey())
                    .append("AutoIncrement=").append(constraints.isAutoIncrement())
                    .append("Nullable=").append(constraints.isNullable())
                    .append("Unique=").append(constraints.isUnique());
        }

        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }
}
