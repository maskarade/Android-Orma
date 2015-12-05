package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.MigrationSchema;

import java.util.List;

public class SchemaData implements MigrationSchema {

    final String tableName;

    final String createTableStatement;

    final List<String> createIndexStatements;

    public SchemaData(String tableName, String createTableStatement, List<String> createIndexStatements) {
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
        this.createIndexStatements = createIndexStatements;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getCreateTableStatement() {
        return createTableStatement;
    }

    @Override
    public List<String> getCreateIndexStatements() {
        return createIndexStatements;
    }
}
