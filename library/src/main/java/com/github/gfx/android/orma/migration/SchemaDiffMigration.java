package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.exception.MigrationAbortException;
import com.github.gfx.android.orma.internal.OrmaUtils;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaDiffMigration {

    static final String TAG = SchemaDiffMigration.class.getSimpleName();

    public void start(SQLiteDatabase db, List<Schema<?>> schemas) {
        long t0 = System.currentTimeMillis();

        Map<String, SQLiteMaster> metadata = loadMetadata(db, schemas);
        List<String> statements = diffAll(schemas, metadata);
        executeStatements(db, statements);

        Log.v(TAG, "migration finished in " + (System.currentTimeMillis() - t0) + "ms");
    }

    @NonNull
    public List<String> diffAll(List<Schema<?>> schemas, Map<String, SQLiteMaster> dbTables) {
        List<String> statements = new ArrayList<>();

        // NOTE: ignore tables which exist only in database
        for (Schema<?> schema : schemas) {
            SQLiteMaster table = dbTables.get(schema.getTableName());
            if (table == null) {
                statements.add(schema.getCreateTableStatement());
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
            throw new MigrationAbortException(e);
        }

        Map<String, ColumnDefinition> toColumns = new LinkedHashMap<>();
        List<ColumnDefinition> intersectionColumns = new ArrayList<>();

        for (ColumnDefinition column : toTable.getColumnDefinitions()) {
            String key = column.toString();
            toColumns.put(key, column);
        }

        for (ColumnDefinition column : fromTable.getColumnDefinitions()) {
            String key = column.toString();
            if (toColumns.containsKey(key)) {
                intersectionColumns.add(column);
            }
        }

        if (intersectionColumns.size() != toTable.getColumnDefinitions().size() ||
                intersectionColumns.size() != fromTable.getColumnDefinitions().size()) {
            return buildRecreateTable(fromTable, toTable, intersectionColumns);
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> buildRecreateTable(CreateTable fromTable, CreateTable toTable,
            Collection<ColumnDefinition> intersectionColumns) {
        List<String> statements = new ArrayList<>();

        String tempTable = "__temp_" + toTable.getTable().getName();

        // create the new table
        StringBuilder createNewTable = new StringBuilder();
        createNewTable.append("CREATE TABLE ");
        createNewTable.append(OrmaUtils.quote(tempTable));
        createNewTable.append(" (");
        createNewTable.append(OrmaUtils.joinBy(", ", toTable.getColumnDefinitions(),
                new OrmaUtils.Func1<ColumnDefinition, String>() {
                    @Override
                    public String call(ColumnDefinition arg) {
                        String columnSpec = arg.getColumnSpecStrings() != null ? TextUtils
                                .join(" ", arg.getColumnSpecStrings()) : "";
                        return OrmaUtils.quote(arg.getColumnName()) + ' ' + arg.getColDataType() + columnSpec;
                    }
                }));
        createNewTable.append(")");
        statements.add(createNewTable.toString());

        // insert into the new table
        StringBuilder insertIntoNewTable = new StringBuilder();
        insertIntoNewTable.append("INSERT INTO ");
        insertIntoNewTable.append(OrmaUtils.quote(tempTable));
        insertIntoNewTable.append(" (");
        String unionColumnNames = OrmaUtils.joinBy(", ", intersectionColumns, new OrmaUtils.Func1<ColumnDefinition, String>() {
            @Override
            public String call(ColumnDefinition arg) {
                return OrmaUtils.quote(arg.getColumnName());
            }
        });
        insertIntoNewTable.append(unionColumnNames);
        insertIntoNewTable.append(") SELECT ");
        insertIntoNewTable.append(unionColumnNames);
        insertIntoNewTable.append(" FROM ");
        insertIntoNewTable.append(OrmaUtils.quote(fromTable.getTable().getName()));
        statements.add(insertIntoNewTable.toString());

        // drop the old table

        StringBuilder dropOldTable = new StringBuilder();
        dropOldTable.append("DROP TABLE ");
        dropOldTable.append(OrmaUtils.quote(toTable.getTable().getName()));
        statements.add(dropOldTable.toString());

        // rename table
        StringBuilder alterTableRename = new StringBuilder();
        alterTableRename.append("ALTER TABLE ");
        alterTableRename.append(OrmaUtils.quote(tempTable));
        alterTableRename.append(" RENAME TO ");
        alterTableRename.append(OrmaUtils.quote(toTable.getTable().getName()));
        statements.add(alterTableRename.toString());

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
            String indexName = OrmaUtils.dequote(matcher.group(1));
            return "DROP INDEX IF EXISTS \"" + indexName + "\"";
        } else {
            return "";
        }
    }

    public void executeStatements(SQLiteDatabase db, List<String> statements) {
        db.beginTransaction();

        try {
            for (String statement : statements) {
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, statement);
                }
                db.execSQL(statement);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, SQLiteMaster> loadMetadata(SQLiteDatabase db, List<Schema<?>> schemas) {
        List<String> tableNames = new ArrayList<>();
        for (Schema<?> schema : schemas) {
            tableNames.add(OrmaUtils.quote(schema.getTableName()));
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

                Log.d(TAG, type + " " + name + " " + tableName + " " + sql);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return tables;
    }


}