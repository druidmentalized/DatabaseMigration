package org.MigrationTool.Parsers;

import org.MigrationTool.Actions.*;
import org.MigrationTool.Main.Migration;
import org.MigrationTool.Models.Column;
import org.MigrationTool.Models.Constraints;
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
        int id = Integer.parseInt(migrationElement.getAttribute("id"));
        String author = migrationElement.getAttribute("author");

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
                String tableName = actionElement.getAttribute("tableName");

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
                String tableName = actionElement.getAttribute("tableName");

                //parsing single column
                Element columnElement = (Element) actionElement.getElementsByTagName("column").item(0);

                Column column = parseColumn(columnElement);

                return new AddColumnAction(tableName, column);
            }
            case "modifyColumnType" -> {
                //todo: make modifying column type
            }
            case "dropColumn" -> {
                //todo: make dropping column
            }
            case "dropTable" -> {
                //todo: make dropping table
            }
            //todo: add some more as an additional task
        }
        return null;
    }

    private Column parseColumn(Element columnElement) {
        String name = columnElement.getAttribute("name");
        String type = columnElement.getAttribute("type");

        Element constraintsElement = (Element) columnElement.getElementsByTagName("constraints").item(0);
        Constraints constraints = new Constraints();
        if (constraintsElement != null) {
            constraints.setPrimaryKey(parseBooleanOrDefault(constraintsElement, "primaryKey", false));
            constraints.setAutoIncrement(parseBooleanOrDefault(constraintsElement, "auto_increment", false));
            constraints.setNullable(parseBooleanOrDefault(constraintsElement, "nullable", false));
            constraints.setUnique(parseBooleanOrDefault(constraintsElement, "unique", false));
        }

        return new Column(name, type, constraints);
    }

    //helper
    private boolean parseBooleanOrDefault(Element element, String tagName, boolean defaultValue) {
        if (element.hasAttribute(tagName)) {
            return Boolean.parseBoolean(element.getAttribute(tagName));
        }
        return defaultValue;
    }
}
