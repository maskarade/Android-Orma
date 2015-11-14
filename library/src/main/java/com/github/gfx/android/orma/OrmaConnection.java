package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class OrmaConnection extends SQLiteOpenHelper {

    static final int VERSION = 1;

    final List<Schema<?>> schemas;

    public OrmaConnection(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas) {
        super(context, filename, null, VERSION);
        this.schemas = schemas;
    }


    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    public <T> long insert(Schema<T> schema, T model) {
        SQLiteDatabase db = getDatabase();
        return db.insertWithOnConflict(
                schema.getTableName(),
                null,
                schema.serializeModelToContentValues(model),
                SQLiteDatabase.CONFLICT_ROLLBACK);
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

    void dropAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            db.execSQL(dropTable(schema));
        }

    }

    void createAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            db.execSQL(createTable(schema));

            // TODO: create indexes
        }
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
        addIdentifier(sb, schema.getTableName());
        sb.append(" (");

        for (ColumnDef<?> column : schema.getColumns()) {
            addColumnDef(sb, column);

            sb.append(", ");
        }

        sb.setLength(sb.length() - ", ".length()); // chop the last ", "

        sb.append(')');

        Log.d("Orma", sb.toString());

        return sb.toString();
    }

    void addColumnDef(StringBuilder sb, ColumnDef<?> column) {
        addIdentifier(sb, column.name);
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

    void addIdentifier(StringBuilder sb, String identifier) {
        sb.append('"');
        sb.append(identifier);
        sb.append('"');
    }

    String dropTable(Schema<?> schema) {
        return "DROP TABLE IF EXISTS " + schema.getTableName();
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
