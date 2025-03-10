package org.MigrationTool.Parsers;

import org.MigrationTool.Actions.*;
import org.MigrationTool.Main.Migration;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraint;
import org.MigrationTool.Models.ConstraintType;
import org.MigrationTool.Utils.AttributeNames;
import org.MigrationTool.Utils.ConstraintNameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                rollbackActions.addAll(parseActions(rollbackActionNodes, id));
            }
            else {
                MigrationAction migrationAction = parseAction(actionElement, id);
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

    private MigrationAction parseAction(Element actionElement, int migrationId) {
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

                    columns.add(parseColumn(columnElement, tableName, migrationId));
                }

                return new CreateTableAction(tableName, columns);
            }
            case "addColumn" -> {

                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName, migrationId);

                return new AddColumnAction(tableName, column);
            }
            case "addConstraint" -> {

            }
            case "addIndex" -> {

            }
            case "addForeignKey" -> {

            }
            case "renameTable" -> {

            }
            case "renameColumn" -> {

            }
            case "modifyColumnType" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName, migrationId);

                return new ModifyColumnTypeAction(tableName, column.getName(), column.getNewDataType());
            }
            case "dropColumn" -> {
                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement, tableName, migrationId);

                return new DropColumnAction(tableName, column.getName());
            }
            case "dropTable" -> {
                return new DropTableAction(tableName);
            }
            case "dropConstraint" -> {

            }
            case "dropIndex" -> {

            }
            case "dropForeignKey" -> {

            }
            default -> logger.error("Unsupported action type: {}", actionType);
            //todo: add some more as an additional task
        }
        return null;
    }

    private List<MigrationAction> parseActions(NodeList rollbackActionNodes, int migrationId) {
        List<MigrationAction> rollbackActions = new ArrayList<>();

        for (int k = 0; k < rollbackActionNodes.getLength(); k++) {
            Node rollbackActionNode = rollbackActionNodes.item(k);
            if (rollbackActionNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element rollbackActionElement = (Element) rollbackActionNode;

            MigrationAction rollbackAction = parseAction(rollbackActionElement, migrationId);
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

    private Column parseColumn(Element columnElement, String tableName, int migrationId) {
        Column column = new Column();
        column.setName(parseStringOrDefault(columnElement, AttributeNames.columnName, ""));
        column.setType(parseStringOrDefault(columnElement, AttributeNames.columnType, ""));
        column.setNewDataType(parseStringOrDefault(columnElement, AttributeNames.newDataType, ""));

        NodeList constraintsNodes = columnElement.getElementsByTagName("constraint");
        for (int i = 0; i < constraintsNodes.getLength(); i++) {
            Element constraintElement = (Element) constraintsNodes.item(i);
            Constraint constraint = parseConstraint(constraintElement, tableName, column.getName(), migrationId);
            column.getConstraintsList().add(constraint);
        }

        if (column.getConstraintsList().isEmpty()) {
            logger.debug("No constraints specified for column '{}'", column.getName());
        } else {
            logger.debug("Parsed {} constraints for column {}", column.getConstraintsList().size(), column.getName());
        }

        return column;
    }

    private Constraint parseConstraint(Element constraintElement, String tableName, String columnName, int migrationId) {
        Constraint constraint = new Constraint();

        try {
            //getting type
            ConstraintType type = ConstraintType.valueOf(constraintElement.getAttribute("type").toUpperCase());
            constraint.setType(type);

            //generating name
            String name = ConstraintNameGenerator.generateConstraintName(tableName, columnName, type.toString(), migrationId);
            constraint.setName(name);

            if (type == ConstraintType.CHECK) {
                constraint.setExpression(constraintElement.getAttribute("expression"));
            }
            else {
                constraint.setExpression(columnName);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Unsupported constraint type: {}", constraintElement.getAttribute("type"));
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

}
