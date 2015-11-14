package com.github.gfx.android.orma;

import java.util.List;

public class OrmaSqlGenerator {

    static final int INITIAL_CAPACITY = 100;

    String createTable(Schema<?> schema) {
        StringBuilder sb = new StringBuilder(INITIAL_CAPACITY);

        sb.append("CREATE TABLE ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");

        for (ColumnDef<?> column : schema.getColumns()) {
            addColumnDef(sb, column);

            sb.append(", ");
        }

        sb.setLength(sb.length() - ", ".length()); // chop the last ", "

        sb.append(')');

        return sb.toString();
    }

    void addColumnDef(StringBuilder sb, ColumnDef<?> column) {
        appendIdentifier(sb, column.name);
        sb.append(' ');

        sb.append(column.getSqlType());
        sb.append(' ');

        if (column.primaryKey) {
            sb.append("PRIMARY KEY ");
        } else {
            if (column.nullable) {
                sb.append("NULL ");
            } else {
                sb.append("NOT NULL ");
            }
            if (column.unique) {
                sb.append("UNIQUE ");
            }
        }
    }

    public String createIndex(Schema<?> schema, ColumnDef<?> column) {
        StringBuilder sb = new StringBuilder(INITIAL_CAPACITY);

        sb.append("CREATE INDEX ");
        appendIdentifier(sb, "index_" + column.name + "_on_" + schema.getTableName());
        sb.append(" ON ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");
        appendIdentifier(sb, column.name);
        sb.append(")");
        return sb.toString();
    }

    public String dropTable(Schema<?> schema) {
        StringBuilder sb = new StringBuilder(INITIAL_CAPACITY);
        sb.append("DROP TABLE IF EXISTS ");
        appendIdentifier(sb, schema.getTableName());
        return sb.toString();
    }


    public String insert(Schema<?> schema) {
        StringBuilder sb = new StringBuilder(INITIAL_CAPACITY);

        sb.append("INSERT OR ROLLBACK INTO ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");

        List<ColumnDef<?>> columns = schema.getColumns();
        int nColumns = columns.size();
        for (int i = 0; i < nColumns; i++) {
            ColumnDef<?> c = columns.get(i);
            if (c.autoId) {
                continue;
            }
            appendIdentifier(sb, c.name);
            if ((i + 1) != nColumns && !columns.get(i + 1).autoId) {
                sb.append(',');
            }
        }
        sb.append(')');
        sb.append(" VALUES (");
        for (int i = 0; i < nColumns; i++) {
            ColumnDef<?> c = columns.get(i);
            if (c.autoId) {
                continue;
            }
            sb.append('?');
            if ((i + 1) != nColumns && !columns.get(i + 1).autoId) {
                sb.append(',');
            }
        }
        sb.append(')');

        return sb.toString();
    }

    public String identifier(String identifier) {
        StringBuilder sb =  new StringBuilder(identifier.length() + 2);
        appendIdentifier(sb, identifier);
        return sb.toString();
    }

    public void appendIdentifier(StringBuilder sb, String identifier) {
        sb.append('"');
        sb.append(identifier);
        sb.append('"');
    }
}
