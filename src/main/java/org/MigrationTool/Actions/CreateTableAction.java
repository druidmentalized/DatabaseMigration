package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
import org.MigrationTool.Utils.ChecksumGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class CreateTableAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(CreateTableAction.class);
    private final String tableName;
    private final List<Column> columns;

    public CreateTableAction(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public void execute() {
        logger.info("Executing CreateTableAction on table: {}", tableName);
        StringBuilder query = new StringBuilder("CREATE TABLE ")
                .append(tableName)
                .append(" (");

        for (int i = 0; i < columns.size(); i++) {
            query.append(columns.get(i).toString());

            if (i < columns.size() - 1) {
                query.append(", ");
            }
        }

        query.append(");");

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query.toString());
            logger.info("Table '{}' successfully created", tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing CreateTableAction for table: " + tableName, e);
        }
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
