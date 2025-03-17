package org.migrationTool.parsers;

import org.migrationTool.actions.*;
import org.migrationTool.main.Migration;
import org.migrationTool.models.Column;
import org.migrationTool.models.Constraint;
import org.migrationTool.models.ConstraintType;
import org.migrationTool.models.Index;
import org.migrationTool.utils.AttributeNames;
import org.migrationTool.utils.NameGenerator;
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
                logger.info("");
                logger.info("────────────────────────────────────────────────────────────────────────────────────");
                Element migrationElement = (Element) migrationNodes.item(i);
                Migration migration = parseMigration(migrationElement);
                migrations.add(migration);
                logger.info("Parsed migration: id={}, author={}", migration.getId(), migration.getAuthor());
            }

            logger.info("");
        }
        catch (Exception e) {
            logger.error("Failed to parse migrations: {}", e.getMessage(), e);
            return null;
        }

        logger.info("Parsing migrations completed successfully.");
        return migrations;
    }

    private Migration parseMigration(Element migrationElement) {
        int id = Integer.parseInt(migrationElement.getAttribute(AttributeNames.id));
        String author = migrationElement.getAttribute(AttributeNames.author);
        logger.debug("Parsing migration: ID={}, Author={}", id, author);

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
                logger.debug("   ├── Parsing rollback actions...");
                rollbackActions.addAll(parseActions(actionElement.getChildNodes()));
            }
            else {
                MigrationAction migrationAction = parseAction(actionElement);
                if (migrationAction != null) {
                    migrationActions.add(migrationAction);
                    logger.info("   ┌── Parsed action: {}", migrationAction.getClass().getSimpleName());
                }
                else {
                    logger.warn("Unknown action: {}", actionElement.getTagName());
                }
            }
        }

        return new Migration(id, author, migrationActions, rollbackActions);
    }

    private MigrationAction parseAction(Element actionElement) {
        String actionType = actionElement.getTagName();
        String tableName = actionElement.getAttribute(AttributeNames.tableName);
        logger.debug("   └── Parsing action: Type={}, Table={}", actionType, tableName);

        return switch (actionType) {
            case "createTable"      -> parseCreateTableAction(actionElement, tableName);
            case "addColumn"        -> parseAddColumnAction(actionElement, tableName);
            case "addConstraint"    -> parseAddConstraintAction(actionElement, tableName);
            case "addIndex"         -> parseAddIndexAction(actionElement, tableName);
            case "renameTable"      -> parseRenameTableAction(actionElement, tableName);
            case "renameColumn"     -> parseRenameColumnAction(actionElement, tableName);
            case "modifyColumnType" -> parseModifyColumnTypeAction(actionElement, tableName);
            case "dropColumn"       -> parseDropColumnAction(actionElement, tableName);
            case "dropTable"        -> new DropTableAction(tableName);
            case "dropConstraint"   -> parseDropConstraintAction(actionElement, tableName);
            case "dropIndex"        -> parseDropIndexAction(actionElement, tableName);
            default -> {
                logger.error("Unsupported action type: {}", actionType);
                yield null;
            }
        };
    }

    private MigrationAction parseCreateTableAction(Element actionElement, String tableName) {
        List<Column> columns = new ArrayList<>();
        NodeList columnNodes = actionElement.getElementsByTagName("column");
        for (int i = 0; i < columnNodes.getLength(); i++) {
            columns.add(parseColumn((Element) columnNodes.item(i), tableName));
        }
        return new CreateTableAction(columns);
    }

    private MigrationAction parseAddColumnAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new AddColumnAction(column);
    }

    private MigrationAction parseAddConstraintAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new AddConstraintAction(column.getConstraintsList().getFirst());
    }

    private MigrationAction parseAddIndexAction(Element actionElement, String tableName) {
        Element indexElement = (Element) actionElement.getElementsByTagName("index").item(0);
        Index index = parseIndex(indexElement, tableName);
        return new AddIndexAction(index);
    }

    private MigrationAction parseRenameTableAction(Element actionElement, String tableName) {
        String newTableName = actionElement.getAttribute(AttributeNames.newTableName);
        return new RenameTableAction(tableName, newTableName);
    }

    private MigrationAction parseRenameColumnAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new RenameColumnAction(column);
    }

    private MigrationAction parseModifyColumnTypeAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new ModifyColumnTypeAction(column);
    }

    private MigrationAction parseDropColumnAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new DropColumnAction(column);
    }

    private MigrationAction parseDropConstraintAction(Element actionElement, String tableName) {
        Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);
        Column column = parseColumn(columnElement, tableName);
        return new DropConstraintAction(column.getConstraintsList().getFirst());
    }

    private MigrationAction parseDropIndexAction(Element actionElement, String tableName) {
        Element indexElement = (Element) actionElement.getElementsByTagName("index").item(0);
        Index index = parseIndex(indexElement, tableName);
        return new DropIndexAction(index);
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
                logger.debug("   ┌── Parsed rollback action: {}", rollbackAction.getClass().getSimpleName());
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
        column.setTableName(tableName);
        column.setType(parseStringOrDefault(columnElement, AttributeNames.columnType, ""));
        column.setNewDataType(parseStringOrDefault(columnElement, AttributeNames.newDataType, ""));
        column.setNewName(parseStringOrDefault(columnElement, AttributeNames.newColumnName, ""));

        logger.debug("      └── Parsing column: Name={}", column.getName());

        NodeList constraintsNodes = columnElement.getElementsByTagName("constraint");
        for (int i = 0; i < constraintsNodes.getLength(); i++) {
            Element constraintElement = (Element) constraintsNodes.item(i);
            Constraint constraint = parseConstraint(constraintElement, tableName, column.getName());
            column.getConstraintsList().add(constraint);
        }

        logger.debug("      ┌── Parsed column {} with {} constraints", column.getName(), column.getConstraintsList().size());

        return column;
    }

    private Index parseIndex(Element indexElement, String tableName) {
        Index index = new Index();
        index.setUnique(parseBooleanOrDefault(indexElement, AttributeNames.indexUniqueness, false));
        index.setTableName(tableName);

        List<String> columnNames = index.getColumns();
        NodeList columnNodes = indexElement.getElementsByTagName("column");
        for (int i = 0; i < columnNodes.getLength(); i++) {
            String columnName = parseStringOrDefault((Element) columnNodes.item(i), AttributeNames.columnName, "");

            if (!columnName.isEmpty()) {
                columnNames.add(columnName);
            }
        }

        index.setName(NameGenerator.generateIndexName(tableName, columnNames));

        logger.debug("          └── Index: Unique={}, Name={}", index.isUnique(), index.getName());

        return index;
    }

    private Constraint parseConstraint(Element constraintElement, String tableName, String columnName) {
        Constraint constraint = new Constraint();
        constraint.setTableName(tableName);

        ConstraintType type = ConstraintType.valueOf(constraintElement.getAttribute(AttributeNames.constraintType).toUpperCase());
        constraint.setType(type);

        constraint.setName(NameGenerator.generateConstraintName(tableName, columnName, type.toString()));
        constraint.setExpression(parseStringOrDefault(constraintElement, AttributeNames.expression, ""));
        constraint.setColumnName(columnName);

        logger.debug("          └── Constraint: Type={}, Name={}", type, constraint.getName());

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
