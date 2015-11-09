package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class OrmaCore extends SQLiteOpenHelper {

    static final int VERSION = 1;

    final List<Schema<?>> schemas;

    public OrmaCore(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas) {
        super(context, filename, null, VERSION);
        this.schemas = schemas;
    }


    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    public long insert(String table, ContentValues values) {
        // TODO: support transaction blocks
        SQLiteDatabase db = getDatabase();
        return db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
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
        return db.query(table, columns, whereClause, whereArgs, groupBy, having, orderBy, limit);
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
        transaction(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                createAllTables(db);
            }
        });
    }

    // https://www.sqlite.org/lang_createtable.html
    String createTable(Schema<?> schema) {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE ");
        addIdentifier(sb, schema.getTableName());
        sb.append(" (");

        for (Column<?> column : schema.getColumns()) {
            addColumnDef(sb, column);

            sb.append(", ");
        }

        sb.setLength(sb.length() - ", ".length()); // chop the last ", "

        sb.append(')');

        Log.d("Orma", sb.toString());

        return sb.toString();
    }

    void addColumnDef(StringBuilder sb, Column<?> column) {
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
