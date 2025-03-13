package org.MigrationTool.Parsers;

import org.MigrationTool.Actions.*;
import org.MigrationTool.Main.Migration;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Models.ConstraintType;
import org.MigrationTool.Models.Index;
import org.MigrationTool.Utils.AttributeNames;
import org.MigrationTool.Utils.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class MigrationParser {
    private static final Logger logger = LoggerFactory.getLogger(MigrationParser.class);

    public MigrationParser() {}

    public List<Migration> parseMigrations(String filePath) {
        List<Migration> migrations = new ArrayList<>();
        logger.info("Parsing migrations from file: {}", filePath);

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath);

            document.getDocumentElement().normalize();

            //taking each single migration
            NodeList migrationNodes = document.getElementsByTagName("migration");
            logger.debug("Found {} migrations in file", migrationNodes.getLength());

            for (int i = 0; i < migrationNodes.getLength(); i++) {
                Element migrationElement = (Element) migrationNodes.item(i);
                Migration migration = parseMigration(migrationElement);
                migrations.add(migration);
                logger.debug("Parsed migration: id={}, author={}", migration.getId(), migration.getAuthor());
            }
        }
        catch (Exception e) {
            logger.error("Failed to parse migrations: {}", e.getMessage(), e);
            return null;
        }

        return migrations;
    }

    private Migration parseMigration(Element migrationElement) {
        int id = Integer.parseInt(migrationElement.getAttribute(AttributeNames.id));
        String author = migrationElement.getAttribute(AttributeNames.author);
        logger.debug("Parsing migration id={}, author={}", id, author);

        //list for all possible actions
        List<MigrationAction> migrationActions = new ArrayList<>();
        List<MigrationAction> rollbackActions = new ArrayList<>();

        //going through all actions in single migration
        NodeList actionNodes = migrationElement.getChildNodes();
        for (int j = 0; j < actionNodes.getLength(); j++) {
            Node actionNode = actionNodes.item(j);
            if (actionNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element actionElement = (Element) actionNode;

            //parsing rollback info(if exists)
            if (actionElement.getTagName().equals("rollback")) {
                NodeList rollbackActionNodes = actionElement.getChildNodes();
                rollbackActions.addAll(parseActions(rollbackActionNodes));
            }
            else {
                MigrationAction migrationAction = parseAction(actionElement);
                if (migrationAction != null) {
                    migrationActions.add(migrationAction);
                    logger.debug("Parsed action: {}", migrationAction.getClass().getSimpleName());
                }
                else {
                    logger.warn("Unknown action faced: {}", actionElement.getTagName());
                }
            }
        }

        return new Migration(id, author, migrationActions, rollbackActions);
    }

    private MigrationAction parseAction(Element actionElement) {
        String actionType = actionElement.getTagName();
        String tableName = actionElement.getAttribute(AttributeNames.tableName);
        logger.debug("Parsing action type '{}' on table '{}' ", actionType, tableName);

        switch (actionType) {
            case "createTable" -> {
                //parsing columns
                List<Column> columns = new ArrayList<>();
                NodeList columnNodes = actionElement.getElementsByTagName("column");
                for (int i = 0; i < columnNodes.getLength(); i++) {
                    Element columnElement = (Element) columnNodes.item(i);

                    columns.add(parseColumn(columnElement, tableName));
                }

                return new CreateTableAction(tableName, columns);
            }
            case "addColumn" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new AddColumnAction(tableName, column);
            }
            case "addConstraint" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new AddConstraintAction(tableName, column.getConstraintsList().getFirst());
            }
            case "addIndex" -> {
                //parsing index
                Element indexElement = (Element) actionElement.getElementsByTagName("index").item(0);

                Index index = parseIndex(indexElement, tableName);

                return new AddIndexAction(tableName, index);
            }
            case "renameTable" -> {
                String newTableName = actionElement.getAttribute(AttributeNames.newTableName);

                return new RenameTableAction(tableName, newTableName);
            }
            case "renameColumn" -> {
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new RenameColumnAction(tableName, column.getName(), column.getNewColumnName());
            }
            case "modifyColumnType" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new ModifyColumnTypeAction(tableName, column.getName(), column.getNewDataType());
            }
            case "dropColumn" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new DropColumnAction(tableName, column.getName());
            }
            case "dropTable" -> {
                return new DropTableAction(tableName);
            }
            case "dropConstraint" -> {
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName);

                return new DropConstraintAction(tableName, column.getConstraintsList().getFirst());
            }
            case "dropIndex" -> {
                //parsing index
                Element indexElement = (Element) actionElement.getElementsByTagName("index").item(0);

                Index index = parseIndex(indexElement, tableName);

                return new DropIndexAction(tableName, index);
            }
            default -> logger.error("Unsupported action type: {}", actionType);
        }
        return null;
    }

    private List<MigrationAction> parseActions(NodeList rollbackActionNodes) {
        List<MigrationAction> rollbackActions = new ArrayList<>();

        for (int k = 0; k < rollbackActionNodes.getLength(); k++) {
            Node rollbackActionNode = rollbackActionNodes.item(k);
            if (rollbackActionNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element rollbackActionElement = (Element) rollbackActionNode;

            MigrationAction rollbackAction = parseAction(rollbackActionElement);
            if (rollbackAction != null) {
                rollbackActions.add(rollbackAction);
                logger.debug("Parsed rollback action: {}", rollbackAction.getClass().getSimpleName());
            }
            else {
                logger.warn("Unknown action faced: {}", rollbackActionElement.getTagName());
            }
        }

        return rollbackActions;
    }

    private Column parseColumn(Element columnElement, String tableName) {
        Column column = new Column();
        column.setName(parseStringOrDefault(columnElement, AttributeNames.columnName, ""));
        column.setType(parseStringOrDefault(columnElement, AttributeNames.columnType, ""));
        column.setNewDataType(parseStringOrDefault(columnElement, AttributeNames.newDataType, ""));
        column.setNewColumnName(parseStringOrDefault(columnElement, AttributeNames.newColumnName, ""));

        NodeList constraintsNodes = columnElement.getElementsByTagName("constraint");
        for (int i = 0; i < constraintsNodes.getLength(); i++) {
            Element constraintElement = (Element) constraintsNodes.item(i);
            Constraint constraint = parseConstraint(constraintElement, tableName, column.getName());
            column.getConstraintsList().add(constraint);
        }

        if (column.getConstraintsList().isEmpty()) {
            logger.debug("No constraints specified for column '{}'", column.getName());
        } else {
            logger.debug("Parsed {} constraints for column {}", column.getConstraintsList().size(), column.getName());
        }

        return column;
    }

    private Index parseIndex(Element indexElement, String tableName) {
        Index index = new Index();
        index.setUnique(parseBooleanOrDefault(indexElement, AttributeNames.indexUniqueness, false));

        List<String> columnNames = index.getColumns();
        NodeList columnNodes = indexElement.getElementsByTagName("column");
        for (int i = 0; i < columnNodes.getLength(); i++) {
            String columnName = parseStringOrDefault((Element) columnNodes.item(i), AttributeNames.columnName, "");

            if (!columnName.isEmpty()) {
                columnNames.add(columnName);
            }
        }

        index.setName(NameGenerator.generateIndexName(tableName, columnNames));

        return index;
    }

    private Constraint parseConstraint(Element constraintElement, String tableName, String columnName) {
        Constraint constraint = new Constraint();

        try {
            //getting type
            ConstraintType type = ConstraintType.valueOf(constraintElement.getAttribute(AttributeNames.constraintType).toUpperCase());
            constraint.setType(type);

            //generating name
            String name = NameGenerator.generateConstraintName(tableName, columnName, type.toString());
            constraint.setName(name);

            constraint.setExpression(parseStringOrDefault(constraintElement, AttributeNames.expression, ""));
            constraint.setColumnName(columnName);

        } catch (IllegalArgumentException e) {
            logger.error("Unsupported constraint type: {}", constraintElement.getAttribute(AttributeNames.constraintType));
            throw new RuntimeException("Unsupported constraint type", e);
        }

        return constraint;
    }

    //helper
    private String parseStringOrDefault(Element element, String attributeName, String defaultValue) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        return defaultValue;
    }

    private boolean parseBooleanOrDefault(Element element, String attributeName, boolean defaultValue) {
        if (element.hasAttribute(attributeName)) {
            return Boolean.parseBoolean(element.getAttribute(attributeName));
        }
        return defaultValue;
    }
}
