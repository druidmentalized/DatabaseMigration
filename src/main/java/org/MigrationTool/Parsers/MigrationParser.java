package org.MigrationTool.Parsers;

import org.MigrationTool.Actions.*;
import org.MigrationTool.Main.Migration;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
import org.MigrationTool.Utils.AttributeNames;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class MigrationParser {
    public MigrationParser() {}

    public List<Migration> parseMigrations(String filePath) {
        List<Migration> migrations = new ArrayList<>();

        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath);

            document.getDocumentElement().normalize();

            //taking each single migration
            NodeList migrationNodes = document.getElementsByTagName("migration");
            for (int i = 0; i < migrationNodes.getLength(); i++) {
                Element migrationElement = (Element) migrationNodes.item(i);

                migrations.add(parseMigration(migrationElement));
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return migrations;
    }

    private Migration parseMigration(Element migrationElement) {
        int id = Integer.parseInt(migrationElement.getAttribute(AttributeNames.id));
        String author = migrationElement.getAttribute(AttributeNames.author);

        //list for all possible actions
        List<MigrationAction> migrationActions = new ArrayList<>();

        //going through all actions in single migration
        NodeList actionNodes = migrationElement.getChildNodes();
        for (int j = 0; j < actionNodes.getLength(); j++) {
            Node actionNode = actionNodes.item(j);
            if (actionNode.getNodeType() != Node.ELEMENT_NODE) continue;
            Element actionElement = (Element) actionNode;

            migrationActions.add(parseAction(actionElement));
        }

        return new Migration(id, author, migrationActions);
    }

    private MigrationAction parseAction(Element actionElement) {
        switch (actionElement.getTagName()) {
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
            //todo: add some more as an additional task
        }
        return null;
    }

    private Column parseColumn(Element columnElement) {
        Column column = new Column();
        column.setName(parseStringOrDefault(columnElement, AttributeNames.columnName, ""));
        column.setType(parseStringOrDefault(columnElement, AttributeNames.columnType, ""));
        column.setNewDataType(parseStringOrDefault(columnElement, AttributeNames.newDataType, ""));


        Element constraintsElement = (Element) columnElement.getElementsByTagName("constraints").item(0);
        Constraints constraints = new Constraints();
        if (constraintsElement != null) {
            constraints.setPrimaryKey(parseBooleanOrDefault(constraintsElement, AttributeNames.primaryKey, false));
            constraints.setAutoIncrement(parseBooleanOrDefault(constraintsElement, AttributeNames.autoIncrement, false));
            constraints.setNullable(parseBooleanOrDefault(constraintsElement, AttributeNames.nullable, false));
            constraints.setUnique(parseBooleanOrDefault(constraintsElement, AttributeNames.unique, false));
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
