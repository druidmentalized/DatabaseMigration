package org.MigrationTool.Parsers;

import org.MigrationTool.Actions.*;
import org.MigrationTool.Main.Migration;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
import org.MigrationTool.Utils.AttributeNames;
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
        logger.debug("Parsing action type: {}", actionType);

        switch (actionType) {
            case "createTable" -> {
                String tableName = actionElement.getAttribute(AttributeNames.tableName);

                //parsing columns
                List<Column> columns = new ArrayList<>();
                NodeList columnNodes = actionElement.getElementsByTagName("column");
                for (int i = 0; i < columnNodes.getLength(); i++) {
                    Element columnElement = (Element) columnNodes.item(i);

                    columns.add(parseColumn(columnElement));
                }

                return new CreateTableAction(tableName, columns);
            }
            case "addColumn" -> {
                String tableName = actionElement.getAttribute(AttributeNames.tableName);

                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement);

                return new AddColumnAction(tableName, column);
            }
            case "modifyColumnType" -> {
                String tableName = actionElement.getAttribute(AttributeNames.tableName);

                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement);

                return new ModifyColumnTypeAction(tableName, column.getName(), column.getNewDataType());
            }
            case "dropColumn" -> {
                String tableName = actionElement.getAttribute(AttributeNames.tableName);

                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement);

                return new DropColumnAction(tableName, column.getName());
            }
            case "dropTable" -> {
                String tableName = actionElement.getAttribute(AttributeNames.tableName);
                return new DropTableAction(tableName);
            }
            default -> logger.error("Unsupported action type: {}", actionType);
            //todo: add some more as an additional task
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
                logger.debug("Parsed action: {}", rollbackAction.getClass().getSimpleName());
            }
            else {
                logger.warn("Unknown action faced: {}", rollbackActionElement.getTagName());
            }
        }

        return rollbackActions;
    }

    private Column parseColumn(Element columnElement) {
        Column column = new Column();
        column.setName(parseStringOrDefault(columnElement, AttributeNames.columnName, ""));
        column.setType(parseStringOrDefault(columnElement, AttributeNames.columnType, ""));
        column.setNewDataType(parseStringOrDefault(columnElement, AttributeNames.newDataType, ""));

        logger.debug("Parsing column: name={}, type={}, newType={}",
                column.getName(), column.getType(), column.getNewDataType());

        Element constraintsElement = (Element) columnElement.getElementsByTagName("constraints").item(0);
        Constraints constraints = column.getConstraints();
        if (constraintsElement != null) {
            constraints.setPrimaryKey(parseBooleanOrDefault(constraintsElement, AttributeNames.primaryKey, false));
            constraints.setAutoIncrement(parseBooleanOrDefault(constraintsElement, AttributeNames.autoIncrement, false));
            constraints.setNullable(parseBooleanOrDefault(constraintsElement, AttributeNames.nullable, false));
            constraints.setUnique(parseBooleanOrDefault(constraintsElement, AttributeNames.unique, false));
            logger.debug("Parsed constraints for column '{}': {}", column.getName(), constraints);
        }
        else {
            logger.debug("No constraints specified for column '{}'", column.getName());
        }

        return column;
    }

    //helper
    private boolean parseBooleanOrDefault(Element element, String attributeName, boolean defaultValue) {
        if (element.hasAttribute(attributeName)) {
            return Boolean.parseBoolean(element.getAttribute(attributeName));
        }
        return defaultValue;
    }

    private String parseStringOrDefault(Element element, String attributeName, String defaultValue) {
        if (element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        return defaultValue;
    }
}
