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

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaDiffMigration implements MigrationEngine {

    static final String TAG = "SchemaDiffMigration";

    final boolean trace;

    final int revision;

    final SqliteDdlBuilder util = new SqliteDdlBuilder();

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

    /**
     * @param schemaIndexes Set of "CREATE INDEX" statements which the code has
     * @param dbIndexes Set of "CREATED INDEX" statements which the DB has
     * @return List of "CREATED INDEX" statements to apply to DB
     */
    @NonNull
    public List<String> indexDiff(@NonNull Set<String> schemaIndexes, @NonNull Set<String> dbIndexes) {
        List<String> createIndexStatements = new ArrayList<>();

        Map<SQLiteComponent, String> unionIndexes = new LinkedHashMap<>();

        Map<SQLiteComponent, String> fromIndexPairs = new LinkedHashMap<>();
        for (String createIndexStatement : dbIndexes) {
            fromIndexPairs.put(SQLiteParserUtils.parseIntoSQLiteComponent(createIndexStatement), createIndexStatement);
        }
        unionIndexes.putAll(fromIndexPairs);

        Map<SQLiteComponent, String> toIndexPairs = new LinkedHashMap<>();
        for (String createIndexStatement : schemaIndexes) {
            toIndexPairs.put(SQLiteParserUtils.parseIntoSQLiteComponent(createIndexStatement), createIndexStatement);
        }
        unionIndexes.putAll(toIndexPairs);

        for (Map.Entry<SQLiteComponent, String> createIndexStatement : unionIndexes.entrySet()) {
            boolean existsInDst = toIndexPairs.containsKey(createIndexStatement.getKey());
            boolean existsInSrc = fromIndexPairs.containsKey(createIndexStatement.getKey());

            if (existsInDst && existsInSrc) {
                // okay, nothing to do
            } else if (existsInDst) {
                createIndexStatements.add(createIndexStatement.getValue());
            } else {
                String statement = buildDropIndexStatement(createIndexStatement.getValue());
                if (!TextUtils.isEmpty(statement)) {
                    createIndexStatements.add(buildDropIndexStatement(createIndexStatement.getValue()));
                }
            }
        }
        return createIndexStatements;
    }

    public List<String> tableDiff(String from, String to) {
        CreateTableStatement fromTable = SQLiteParserUtils.parseIntoCreateTableStatement(from);
        CreateTableStatement toTable = SQLiteParserUtils.parseIntoCreateTableStatement(to);

        Set<CreateTableStatement.ColumnDef> toColumns = new LinkedHashSet<>();
        Set<SQLiteComponent.Name> toColumnNames = new LinkedHashSet<>();
        List<CreateTableStatement.ColumnDef> intersectionColumns = new ArrayList<>();

        List<SQLiteComponent.Name> intersectionColumnNames = new ArrayList<>();

        for (CreateTableStatement.ColumnDef column : toTable.getColumns()) {
            toColumns.add(column);
            toColumnNames.add(column.getName());
        }

        for (CreateTableStatement.ColumnDef column : fromTable.getColumns()) {
            if (toColumns.contains(column)) {
                intersectionColumns.add(column);
            }

            if (toColumnNames.contains(column.getName())) {
                intersectionColumnNames.add(column.getName());
            }
        }

        if (intersectionColumns.size() != toTable.getColumns().size() ||
                intersectionColumns.size() != fromTable.getColumns().size() ||
                !fromTable.getConstraints().equals(toTable.getConstraints())) {
            if (trace) {
                Log.v(TAG, "tables differ:");
                Log.v(TAG, "from: " + from);
                Log.v(TAG, "to:   " + to);
            }
            return buildRecreateTable(fromTable, toTable, intersectionColumnNames);
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> buildRecreateTable(CreateTableStatement fromTable, CreateTableStatement toTable,
            Collection<SQLiteComponent.Name> columns) {
        SQLiteComponent.Name fromTableName = fromTable.getTableName();
        SQLiteComponent.Name toTableName = toTable.getTableName();

        List<String> statements = new ArrayList<>();

        SQLiteComponent.Name tempTableName = new SQLiteComponent.Name("__temp_" + toTableName.getUnquotedToken());

        statements.add(util.buildCreateTable(tempTableName,
                util.map(toTable.getColumns(), new SqliteDdlBuilder.Func<CreateTableStatement.ColumnDef, String>() {
                    @Override
                    public String call(CreateTableStatement.ColumnDef arg) {
                        StringBuilder columnSpecBuilder = new StringBuilder(arg.getName());

                        if (arg.getType() != null) {
                            columnSpecBuilder.append(' ');
                            columnSpecBuilder.append(arg.getType());
                        }

                        if (!arg.getConstraints().isEmpty()) {
                            columnSpecBuilder.append(' ');
                            columnSpecBuilder.append(TextUtils.join(" ", arg.getConstraints()));
                        }
                        return columnSpecBuilder.toString();
                    }
                })));

        statements.add(util.buildInsertFromSelect(tempTableName, fromTableName, columns));
        statements.add(util.buildDropTable(fromTableName));
        statements.add(util.buildRenameTable(tempTableName, toTableName));

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
        db.beginTransaction();

        try {
            for (String statement : statements) {
                if (trace) {
                    Log.v(TAG, statement);
                }
                db.execSQL(statement);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, SQLiteMaster> loadMetadata(SQLiteDatabase db, List<? extends MigrationSchema> schemas) {
        Map<String, SQLiteMaster> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        Set<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (MigrationSchema schema : schemas) {
            tableNames.add(schema.getTableName());
        }

        for (Map.Entry<String, SQLiteMaster> entry : SQLiteMaster.load(db).entrySet()) {
            if (tableNames.contains(entry.getKey())) {
                metadata.put(entry.getKey(), entry.getValue());
            }
        }
        return metadata;
    }
}
