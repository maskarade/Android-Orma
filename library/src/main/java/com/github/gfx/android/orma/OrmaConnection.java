package com.github.gfx.android.orma;

import com.github.gfx.orma.BuildConfig;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class OrmaConnection extends SQLiteOpenHelper {

    static final String TAG = OrmaConnection.class.getSimpleName();

    static final int VERSION = 1;

    final List<Schema<?>> schemas;

    public OrmaConnection(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas) {
        super(context, filename, null, VERSION);
        this.schemas = schemas;
    }


    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    public <T> Inserter<T> prepareInsert(Schema<T> schema) {
        SQLiteDatabase db = getDatabase();
        SQLiteStatement statement = db.compileStatement(buildInsertStatement(schema));
        return new Inserter<>(schema, statement);
    }

    public <T> long insert(Schema<T> schema, T model) {
        Inserter<T> statement = prepareInsert(schema);
        return statement.insert(model);
    }

    public long update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getDatabase();
        return db.updateWithOnConflict(table, values, whereClause, whereArgs, SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    public long count(String table, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getDatabase();
        String[] columns = {"COUNT(*)"};
        Cursor cursor = db.query(table, columns, whereClause, whereArgs, null, null, null);
        try {
            cursor.moveToFirst();
            return cursor.getLong(0);
        } finally {
            cursor.close();
        }
    }

    public Cursor query(String table, String[] columns, String whereClause, String[] whereArgs,
            String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = getDatabase();

        String sql = SQLiteQueryBuilder.buildQueryString(
                false, table, columns, whereClause, groupBy, having, orderBy, limit);

        // To reuse cursor for each query
        SQLiteDatabase.CursorFactory cursorFactory = new SQLiteDatabase.CursorFactory() {
            SQLiteCursor cursor;

            @Override
            public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
                if (cursor == null) {
                    cursor = new SQLiteCursor(driver, editTable, query);
                }
                return cursor;
            }
        };

        return db.rawQueryWithFactory(cursorFactory, sql, whereArgs, table);
    }

    public int delete(@NonNull String table, @Nullable String whereClause, @Nullable String[] whereArgs) {
        SQLiteDatabase db = getDatabase();
        return db.delete(table, whereClause, whereArgs);
    }

    public void transaction(@NonNull TransactionTask task) {
        SQLiteDatabase db = getDatabase();
        db.beginTransaction();

        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new TransactionAbortException(e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Drops and creates all the tables. This is provided for testing.
     */
    public void resetDatabase() {
        transaction(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                SQLiteDatabase db = getDatabase();

                dropAllTables(db);
                onCreate(db);
            }
        });
    }

    String buildInsertStatement(Schema<?> schema) {
        StringBuilder sb = new StringBuilder();

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

    void dropAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            execSQL(db, dropTable(schema));
        }

    }

    void createAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            execSQL(db, createTable(schema));

            for (ColumnDef<?> column : schema.getColumns()) {
                if (column.indexed && !column.primaryKey) {
                    execSQL(db, createIndex(schema, column));
                }
            }
        }
    }

    private void execSQL(SQLiteDatabase db, String sql) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, sql);
        }
        db.execSQL(sql);
    }

    private String createIndex(Schema<?> schema, ColumnDef<?> column) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE INDEX ");
        appendIdentifier(sb, "index_" + column.name + "_on_" + schema.getTableName());
        sb.append(" ON ");
        appendIdentifier(sb, schema.getTableName());
        sb.append(" (");
        appendIdentifier(sb, column.name);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.beginTransaction();

        try {
            createAllTables(db);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new TransactionAbortException(e);
        } finally {
            db.endTransaction();
        }
    }

    // https://www.sqlite.org/lang_createtable.html
    String createTable(Schema<?> schema) {
        StringBuilder sb = new StringBuilder();

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

    void appendIdentifier(StringBuilder sb, String identifier) {
        sb.append('"');
        sb.append(identifier);
        sb.append('"');
    }

    String dropTable(Schema<?> schema) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS ");
        appendIdentifier(sb, schema.getTableName());
        return sb.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO: not yet implemented");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO: not yet implemented");
    }
}
