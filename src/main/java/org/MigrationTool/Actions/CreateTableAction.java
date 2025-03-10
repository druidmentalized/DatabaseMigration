package org.MigrationTool.Actions;

import org.MigrationTool.Database.DatabasePool;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Models.ConstraintType;
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

        //adding columns with simple unnamed constraints
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            query.append(column);

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

        try (Connection connection = DatabasePool.getDataSource().getConnection()) {
            logger.debug("SQL Query: {}", query);
            connection.createStatement().execute(query.toString());
            logger.info("Table '{}' successfully created", tableName);
        } catch (SQLException e) {
            logger.error("SQL Exception: {}", e.getMessage());
            throw new RuntimeException("Error executing CreateTableAction for table: " + tableName, e);
        }
    }

    private void executeNamedConstraints() {
        for (Column column : columns) {
            for (Constraint constraint : column.getConstraintsList()) {
                if (constraint.isNamed()) {
                    String query = String.format("ALTER TABLE %s ADD $s;", tableName, constraint);

                    try (Connection connection = DatabasePool.getDataSource().getConnection()) {
                        logger.debug("Executing constraint SQL: {}", query);
                        connection.createStatement().execute(query);
                        logger.info("Constraint '{}' added successfully", constraint.getName());
                    } catch (SQLException e) {
                        logger.error("SQL Exception: {}", e.getMessage());
                        throw new RuntimeException("Error adding named constraint: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public String generateChecksum() {
        StringBuilder stringBuilder = new StringBuilder();

        //creating signature of this specific action
        stringBuilder.append("CreateTable:").append(tableName).append("|");
        columns.stream()
                .sorted(Comparator.comparing(Column::getName))
                .forEach(stringBuilder::append);

        return ChecksumGenerator.generateWithSHA256(stringBuilder.toString());
    }
}
