package org.MigrationTool.Actions;

import org.MigrationTool.Models.Constraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddConstraintAction implements MigrationAction {
    private static final Logger logger = LoggerFactory.getLogger(AddConstraintAction.class);
    private final String tableName;
    private final Constraint constraint;

    public AddConstraintAction(String tableName, Constraint constraint) {
        this.tableName = tableName;
        this.constraint = constraint;
    }

    @Override
    public void execute() {
        if (constraint.isNamed()) {

        }
        else {

        }
    }

    @Override
    public String generateChecksum() {
        return "";
    }
}
