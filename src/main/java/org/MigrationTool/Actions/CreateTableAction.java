package org.MigrationTool.Actions;

import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
import org.MigrationTool.Utils.ChecksumGenerator;

import java.util.Comparator;
import java.util.List;

public class CreateTableAction implements MigrationAction {
    private String tableName;
    private List<Column> columns;

    public CreateTableAction(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public void execute() {

    }

    @Override
    public String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder();

        //creating signature of this specific action
        stringBuilder.append("CreateTable:").append(tableName).append("|");
        columns.stream()
                .sorted(Comparator.comparing(Column::getName))
                .forEach(column -> {
                    stringBuilder.append(column.getName()).append(column.getType());

                    Constraints constraints = column.getConstraints();
                    if (constraints != null) {
                        stringBuilder.append("PrimaryKey=").append(constraints.isPrimaryKey())
                                     .append("AutoIncrement=").append(constraints.isAutoIncrement())
                                     .append("Nullable=").append(constraints.isNullable())
                                     .append("Unique=").append(constraints.isUnique());
                    }
                });

        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }
}
