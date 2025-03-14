package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraint;
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

    public CreateTableAction(List<Column> columns) {
        this.columns = columns;
        this.tableName = columns.getFirst().getTableName();
    }

    @Override
    public void execute(Connection connection) {
        logger.debug("       Executing CreateTableAction on table: {}", tableName);

        StringBuilder query = new StringBuilder("CREATE TABLE ")
                .append(tableName)
                .append(" (");

        //adding columns with simple unnamed constraints
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            query.append(column.getName()).append(" ").append(column.getType());

            for (Constraint constraint : column.getConstraintsList()) {
                if (!constraint.isNamed()) {
                    query.append(" ").append(constraint);
                }
            }

            if (i < columns.size() - 1) {
                query.append(", ");
            }
        }

        query.append(");");

        try {
            logger.debug("          └── SQL Query: {}", query);
            connection.createStatement().execute(query.toString());
            logger.info("Table '{}' created", tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing CreateTableAction for table: " + tableName, e);
        }

        //adding all named constraints
        for (Column column : columns) {
            for (Constraint constraint : column.getConstraintsList()) {
                if (constraint.isNamed()) {
                    new AddConstraintAction(constraint).execute(connection);
                }
            }
        }
    }

    @Override
    public String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("CreateTable:").append(tableName).append("|");
        columns.forEach(column -> stringBuilder.append(column).append("|"));
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);


        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }
}
