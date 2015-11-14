package com.github.gfx.android.orma;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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

    final OrmaSqlGenerator sql;

    public OrmaConnection(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas) {
        super(context, filename, null, VERSION);
        this.schemas = schemas;
        this.sql = new OrmaSqlGenerator();
    }

    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    public <T> T createModel(Schema<T> schema, ModelBuilder<T> builder) {
        long id = insert(schema, builder.build());
        ColumnDef<?> primaryKey = schema.getPrimaryKey();
        assert primaryKey != null;
        String whereClause = sql.identifier(primaryKey.name) + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return querySingle(schema, schema.getColumnNames(), whereClause, whereArgs, null, null, null);
    }

    public <T> Inserter<T> prepareInsert(Schema<T> schema) {
        SQLiteDatabase db = getDatabase();
        SQLiteStatement statement = db.compileStatement(sql.insert(schema));
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

    public Cursor query(String table, String[] columns, String whereClause, String[] whereArgs,
            String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = getDatabase();
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, table, columns, whereClause, groupBy, having, orderBy, limit);

        return db.rawQueryWithFactory(new CachedCursorFactory(), sql, whereArgs, table);
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

    public <T> T querySingle(Schema<T> schema, String[] columns, String whereClause, String[] whereArgs,
            String groupBy, String having, String orderBy) {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.query(schema.getTableName(), columns, whereClause, whereArgs, groupBy, having, orderBy, "1");

        try {
            if (cursor.moveToFirst()) {
                return schema.createModelFromCursor(this, cursor);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
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
            execSQL(db, sql.dropTable(schema));
        }
    }

    void createAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            execSQL(db, sql.createTable(schema));

            for (ColumnDef<?> column : schema.getColumns()) {
                if (column.indexed && !column.primaryKey) {
                    execSQL(db, sql.createIndex(schema, column));
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO: not yet implemented");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO: not yet implemented");
    }
}
