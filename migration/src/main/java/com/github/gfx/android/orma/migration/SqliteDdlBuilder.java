/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.migration.sqliteparser.CreateTableStatement;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteComponent;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteParserUtils;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqliteDdlBuilder {

    @NonNull
    public static String ensureEscaped(@NonNull String name) {
        return '`' + ensureNotEscaped(name) + '`';
    }

    @NonNull
    public static String ensureNotEscaped(@NonNull String maybeEscaped) {
        if (maybeEscaped.startsWith("\"") || maybeEscaped.endsWith("\n")) {
            return maybeEscaped.substring(1, maybeEscaped.length() - 1);
        } else if (maybeEscaped.startsWith("`") || maybeEscaped.endsWith("`")) {
            return maybeEscaped.substring(1, maybeEscaped.length() - 1);
        } else {
            return maybeEscaped;
        }
    }

    @NonNull
    public String buildCreateTable(@NonNull SQLiteComponent.Name table, @NonNull List<CreateTableStatement.ColumnDef> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(table);
        sb.append(" (");
        appendWithSeparator(sb, ", ", columns);
        sb.append(")");
        return sb.toString();
    }

    @NonNull
    public String buildInsertFromSelect(@NonNull SQLiteComponent.Name fromTable, @NonNull SQLiteComponent.Name toTable,
            @NonNull List<SQLiteComponent.Name> fromColumnNames, @NonNull List<SQLiteComponent.Name> toColumnNames) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(toTable);
        sb.append(" (");
        appendWithSeparator(sb, ", ", toColumnNames);
        sb.append(") SELECT ");
        appendWithSeparator(sb, ", ", fromColumnNames);
        sb.append(" FROM ");
        sb.append(fromTable);

        return sb.toString();
    }

    @NonNull
    public List<String> buildRecreateTable(CreateTableStatement fromTable, CreateTableStatement toTable,
            List<SQLiteComponent.Name> fromColumnNames, List<SQLiteComponent.Name> toColumnNames) {
        SQLiteComponent.Name fromTableName = fromTable.getTableName();
        SQLiteComponent.Name toTableName = toTable.getTableName();

        List<String> statements = new ArrayList<>();

        SQLiteComponent.Name tempTableName = new SQLiteComponent.Name("__temp_" + toTableName.getUnquotedToken());

        statements.add(buildCreateTable(tempTableName, toTable.getColumns()));

        statements.add(buildInsertFromSelect(fromTableName, tempTableName, fromColumnNames, toColumnNames));
        statements.add(buildDropTable(fromTableName));
        statements.add(buildRenameTable(tempTableName, toTableName));
        return statements;
    }


    @NonNull
    public String buildDropTable(@NonNull SQLiteComponent.Name table) {
        return "DROP TABLE " + table;
    }

    @NonNull
    public String buildRenameTable(@NonNull String fromTable, @NonNull String toTable) {
        return buildRenameTable(new SQLiteComponent.Name(fromTable), new SQLiteComponent.Name(toTable));
    }

    @NonNull
    public String buildRenameTable(@NonNull SQLiteComponent.Name fromTable, @NonNull SQLiteComponent.Name toTable) {
        return "ALTER TABLE " + fromTable + " RENAME TO " + toTable;
    }

    @NonNull
    public List<String> buildRenameColumn(@NonNull String table,
            @NonNull String fromColumnName, @NonNull String toColumnName) {
        CreateTableStatement fromTable = SQLiteParserUtils.parseIntoCreateTableStatement(table);
        CreateTableStatement toTable = SQLiteParserUtils.parseIntoCreateTableStatement(table);

        SQLiteComponent.Name fromColumn = new SQLiteComponent.Name(fromColumnName);
        SQLiteComponent.Name toColumn = new SQLiteComponent.Name(toColumnName);

        boolean renamed = false;
        for (CreateTableStatement.ColumnDef column : toTable.getColumns()) {
            if (column.getName().equals(fromColumn)) {
                column.setName(toColumn);
                renamed = true;
            }
        }

        if (!renamed) {
            throw new RuntimeException("No column found in " + fromTable.getTableName() + ": " + fromColumnName);
        }

        return buildRecreateTable(fromTable, toTable,
                extractColumnNames(fromTable.getColumns()), extractColumnNames(toTable.getColumns()));
    }

    private List<SQLiteComponent.Name> extractColumnNames(List<CreateTableStatement.ColumnDef> columns) {
        return map(columns, new Func<CreateTableStatement.ColumnDef, SQLiteComponent.Name>() {
            @Override
            public SQLiteComponent.Name call(CreateTableStatement.ColumnDef arg) {
                return arg.getName();
            }
        });
    }

    @NonNull
    public <A, R> List<R> map(Collection<A> collection, @NonNull Func<A, R> func) {
        List<R> result = new ArrayList<>(collection.size());
        for (A item : collection) {
            result.add(func.call(item));
        }
        return result;
    }

    public <T> void appendWithSeparator(StringBuilder builder, String separator, Collection<T> collection) {
        int i = 0;
        int size = collection.size();
        for (T item : collection) {
            builder.append(item);
            if ((i + 1) != size) {
                builder.append(separator);
            }
            i++;
        }
    }

    public interface Func<A, R> {

        R call(A arg);
    }
}
