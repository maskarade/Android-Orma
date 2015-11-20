package com.github.gfx.android.orma.migration;

import java.util.List;

public class NamedDdl {

    final String tableName;

    final String createTableStatement;

    final List<String> createIndexStatements;

    public NamedDdl(String tableName, String createTableStatement, List<String> createIndexStatements) {
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
        this.createIndexStatements = createIndexStatements;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCreateTableStatement() {
        return createTableStatement;
    }

    public List<String> getCreateIndexStatements() {
        return createIndexStatements;
    }
}
