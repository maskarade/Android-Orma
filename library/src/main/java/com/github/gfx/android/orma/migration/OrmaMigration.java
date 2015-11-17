package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.Schema;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrmaMigration {

    static final String TAG = OrmaMigration.class.getSimpleName();

    public void start(SQLiteDatabase db, List<Schema<?>> schemas) {
        long t0 = System.currentTimeMillis();

        Map<String, SQLiteMaster> metadata = loadMetadata(db, schemas);
        List<String> statements = diff(schemas, metadata);
        executeStatements(db, statements);

        Log.v(TAG, "migration finished in " + (System.currentTimeMillis() - t0) + "ms");
    }

    @NonNull
    public List<String> diff(List<Schema<?>> schemas, Map<String, SQLiteMaster> tables) {
        List<String> statements = new ArrayList<>();

        // ignore tables which exist only in database
        for (Schema<?> schema : schemas) {
            SQLiteMaster table = tables.get(schema.getTableName());
            if (table == null) {
                statements.add(schema.getCreateTableStatement());
            } else {
                if (!table.sql.equals(schema.getCreateTableStatement())) {
                    statements.addAll(tableDiff(table.sql, schema.getCreateTableStatement()));
                }

                Set<String> schemaIndexes = new LinkedHashSet<>();
                Set<String> dbIndexes = new LinkedHashSet<>();
                Set<String> allIndexes = new LinkedHashSet<>();

                schemaIndexes.addAll(schema.getCreateIndexStatements());

                for (SQLiteMaster index : table.indexes) {
                    dbIndexes.add(index.sql);
                }

                allIndexes.addAll(schemaIndexes);
                allIndexes.addAll(dbIndexes);

                for (String createIndexStatement : allIndexes) {
                    boolean existsInSchema = schemaIndexes.contains(createIndexStatement);
                    boolean existsInDb = dbIndexes.contains(createIndexStatement);

                    if (existsInSchema && existsInDb) {
                        // okay, nothing to do
                    } else if (existsInSchema) {
                        statements.add(createIndexStatement);
                    } else { //
                        statements.add(buildDropIndexStatement(createIndexStatement));
                    }
                }

            }
        }
        return statements;
    }

    public List<String> tableDiff(String from, String to) {
        List<String> alters = new ArrayList<>();
        // TODO
        return alters;
    }

    public String buildDropIndexStatement(String createIndexStatement) {
        Pattern indexNamePattern = Pattern.compile(
                "CREATE \\s+ INDEX (?:\\s+ IF \\s+ NOT \\s+ EXISTS)? \\s+ (\\S+) \\s+ ON .+",
                Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.DOTALL);

        Matcher matcher = indexNamePattern.matcher(createIndexStatement);
        if (matcher.matches()) {
            String indexName = dequote(matcher.group(1));
            return "DROP INDEX IF EXISTS \"" + indexName + "\"";
        } else {
            return "";
        }
    }

    public String dequote(String maybeQuoted) {
        if (maybeQuoted.startsWith("\"") || maybeQuoted.endsWith("\n")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else if (maybeQuoted.startsWith("`") || maybeQuoted.endsWith("`")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else {
            return maybeQuoted;
        }
    }


    public void executeStatements(SQLiteDatabase db, List<String> statements) {
        db.beginTransaction();

        try {
            for (String statement : statements) {
                Log.d(TAG, statement);
                //db.execSQL(statement);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Map<String, SQLiteMaster> loadMetadata(SQLiteDatabase db, List<Schema<?>> schemas) {
        List<String> tableNames = new ArrayList<>();
        for (Schema<?> schema : schemas) {
            tableNames.add('"' + schema.getTableName() + '"');
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
                        // attach the index to the table
                        meta.indexes.add(new SQLiteMaster(type, name, tableName, sql));
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
