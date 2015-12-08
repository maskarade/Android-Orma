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

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaDiffMigration implements MigrationEngine {

    static final String TAG = "SchemaDiffMigration";

    final boolean trace;

    final int revision;

    final SqliteDdlBuilder builder = new SqliteDdlBuilder();

    public SchemaDiffMigration(@NonNull Context context, boolean trace) {
        this.revision = extractRevision(context);
        this.trace = trace;
    }

    public SchemaDiffMigration(@NonNull Context context) {
        this(context, BuildConfig.DEBUG);
    }

    static int extractRevision(Context context) {
        PackageManager pm = context.getPackageManager();
        long t;
        try {
            t = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).lastUpdateTime;
            if (t == 0) {
                t = TimeUnit.MINUTES.toMillis(1); // robolectric
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
        return (int) TimeUnit.MILLISECONDS.toMinutes(t);
    }

    @Override
    public int getVersion() {
        return revision;
    }

    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<? extends MigrationSchema> schemas) {
        Map<String, SQLiteMaster> metadata = loadMetadata(db, schemas);
        List<String> statements = diffAll(metadata, schemas);
        executeStatements(db, statements);
    }

    @NonNull
    public List<String> diffAll(@NonNull Map<String, SQLiteMaster> dbTables,
            @NonNull List<? extends MigrationSchema> schemas) {
        List<String> statements = new ArrayList<>();

        // NOTE: ignore tables which exist only in database
        for (MigrationSchema schema : schemas) {
            SQLiteMaster table = dbTables.get(schema.getTableName());
            if (table == null) {
                statements.add(schema.getCreateTableStatement());
                statements.addAll(schema.getCreateIndexStatements());
            } else {
                if (!table.sql.equals(schema.getCreateTableStatement())) {
                    statements.addAll(tableDiff(table.sql, schema.getCreateTableStatement()));
                }

                Set<String> schemaIndexes = new LinkedHashSet<>();
                Set<String> dbIndexes = new LinkedHashSet<>();

                schemaIndexes.addAll(schema.getCreateIndexStatements());

                for (SQLiteMaster index : table.indexes) {
                    dbIndexes.add(index.sql);
                }
                statements.addAll(indexDiff(schemaIndexes, dbIndexes));
            }
        }
        return statements;
    }

    @NonNull
    public List<String> indexDiff(@NonNull Set<String> schemaIndexes, @NonNull Set<String> dbIndexes) {
        //System.out.println("schemaIndexes: " + schemaIndexes);
        //System.out.println("dbIndexes:     " + dbIndexes);

        List<String> createIndexStatements = new ArrayList<>();

        Set<String> unionIndexes = new LinkedHashSet<>();

        unionIndexes.addAll(schemaIndexes);
        unionIndexes.addAll(dbIndexes);

        for (String createIndexStatement : unionIndexes) {
            boolean existsInSchema = schemaIndexes.contains(createIndexStatement);
            boolean existsInDb = dbIndexes.contains(createIndexStatement);

            if (existsInSchema && existsInDb) {
                // okay, nothing to do
            } else if (existsInSchema) {
                createIndexStatements.add(createIndexStatement);
            } else {
                String statement = buildDropIndexStatement(createIndexStatement);
                if (!TextUtils.isEmpty(statement)) {
                    createIndexStatements.add(buildDropIndexStatement(createIndexStatement));
                }
            }
        }
        return createIndexStatements;
    }

    public List<String> tableDiff(String from, String to) {
        CreateTable fromTable;
        CreateTable toTable;

        try {
            fromTable = (CreateTable) CCJSqlParserUtil.parse(from);
            toTable = (CreateTable) CCJSqlParserUtil.parse(to);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        Map<String, ColumnDefinition> toColumns = new HashMap<>();
        Set<String> toColumnNameAndTypes = new HashSet<>();
        List<ColumnDefinition> intersectionColumns = new ArrayList<>();
        Map<String, String> intersectionColumnNameAndTypes = new HashMap<>();

        for (ColumnDefinition column : toTable.getColumnDefinitions()) {
            toColumns.put(column.toString(), column);
            toColumnNameAndTypes.add(column.getColumnName() + ' ' + column.getColDataType());
        }

        for (ColumnDefinition column : fromTable.getColumnDefinitions()) {
            String columnSpec = column.toString();
            if (toColumns.containsKey(columnSpec)) {
                intersectionColumns.add(column);
            }

            String columnNameAndType = column.getColumnName() + ' ' + column.getColDataType();
            if (toColumnNameAndTypes.contains(columnNameAndType)) {
                intersectionColumnNameAndTypes.put(columnNameAndType, column.getColumnName());
            }
        }

        if (intersectionColumns.size() != toTable.getColumnDefinitions().size() ||
                intersectionColumns.size() != fromTable.getColumnDefinitions().size()) {
            return buildRecreateTable(fromTable, toTable, intersectionColumnNameAndTypes.values());
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> buildRecreateTable(CreateTable fromTable, CreateTable toTable,
            Collection<String> intersectionColumns) {

        String fromTableName = fromTable.getTable().getName();
        String toTableName = toTable.getTable().getName();

        List<String> statements = new ArrayList<>();

        String tempTableName = "__temp_" + SqliteDdlBuilder.ensureNotQuoted(toTableName);

        statements.add(builder.buildCreateTable(tempTableName,
                builder.map(toTable.getColumnDefinitions(), new SqliteDdlBuilder.Func<ColumnDefinition, String>() {
                    @Override
                    public String call(ColumnDefinition arg) {
                        String columnSpec;
                        if (arg.getColumnSpecStrings() != null) {
                            columnSpec = ' ' + TextUtils.join(" ", arg.getColumnSpecStrings());
                        } else {
                            columnSpec = "";
                        }
                        return SqliteDdlBuilder.ensureQuoted(arg.getColumnName()) + ' ' + arg.getColDataType() + columnSpec;
                    }
                })));

        statements.add(builder.buildInsertFromSelect(tempTableName, fromTableName, intersectionColumns));
        statements.add(builder.buildDropTable(fromTableName));
        statements.add(builder.buildRenameTable(tempTableName, toTableName));

        return statements;
    }

    public String buildDropIndexStatement(String createIndexStatement) {
        if (TextUtils.isEmpty(createIndexStatement)) {
            throw new AssertionError("No create index statement given");
        }
        Pattern indexNamePattern = Pattern.compile(
                "CREATE \\s+ INDEX (?:\\s+ IF \\s+ NOT \\s+ EXISTS)? \\s+ (\\S+) \\s+ ON .+",
                Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.DOTALL);

        Matcher matcher = indexNamePattern.matcher(createIndexStatement);
        if (matcher.matches()) {
            String indexName = SqliteDdlBuilder.ensureNotQuoted(matcher.group(1));
            return "DROP INDEX IF EXISTS \"" + indexName + "\"";
        } else {
            return "";
        }
    }

    public void executeStatements(SQLiteDatabase db, List<String> statements) {
        if (trace) {
            for (String statement : statements) {
                Log.v(TAG, statement);
            }
        }

        db.beginTransaction();

        try {
            for (String statement : statements) {
                db.execSQL(statement);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, SQLiteMaster> loadMetadata(SQLiteDatabase db, List<? extends MigrationSchema> schemas) {
        List<String> tableNames = new ArrayList<>();
        for (MigrationSchema schema : schemas) {
            tableNames.add(SqliteDdlBuilder.ensureQuoted(schema.getTableName()));
        }
        Cursor cursor = db.rawQuery(
                "SELECT type,name,tbl_name,sql FROM sqlite_master WHERE tbl_name IN "
                        + "(" + TextUtils.join(", ", tableNames) + ")",
                null);

        Map<String, SQLiteMaster> tables = new HashMap<>();
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(0); // "table" or "index"
                String name = cursor.getString(1); // table or index name
                String tableName = cursor.getString(2);
                String sql = cursor.getString(3);

                SQLiteMaster meta = tables.get(tableName);
                if (meta == null) {
                    meta = new SQLiteMaster();
                    tables.put(tableName, meta);
                }

                switch (type) {
                    case "table":
                        meta.type = type;
                        meta.name = name;
                        meta.tableName = tableName;
                        meta.sql = sql;
                        break;
                    case "index":
                        // sql=null for sqlite_autoindex_${table}_${columnIndex}
                        if (sql != null) {
                            meta.indexes.add(new SQLiteMaster(type, name, tableName, sql));
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("FIXME: unsupported type: " + type);
                }

                tables.put(name, new SQLiteMaster(type, name, tableName, sql));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return tables;
    }
}
