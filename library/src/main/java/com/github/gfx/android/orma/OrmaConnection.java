package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.TransactionAbortException;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.NamedDdl;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class OrmaConnection extends SQLiteOpenHelper {

    static final String TAG = OrmaConnection.class.getSimpleName();

    static final int VERSION = 1;

    final List<Schema<?>> schemas;

    final MigrationEngine migration;

    public OrmaConnection(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas) {
        this(context, filename, schemas, new SchemaDiffMigration(context));
    }

    public OrmaConnection(@NonNull Context context, @Nullable String filename, List<Schema<?>> schemas,
            MigrationEngine migration) {
        super(context, filename, null, migration.getVersion());
        this.schemas = schemas;
        this.migration = migration;
        enableWal();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void enableWal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
        }
    }

    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }

    public <T> T createModel(Schema<T> schema, ModelBuilder<T> builder) {
        long id = insert(schema, builder.build());
        ColumnDef<?> primaryKey = schema.getPrimaryKey();
        assert primaryKey != null;
        String whereClause = '"' + primaryKey.name + '"' + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return querySingle(schema, schema.getEscapedColumnNames(), whereClause, whereArgs, null, null, null);
    }

    public <T> Inserter<T> prepareInsert(Schema<T> schema) {
        SQLiteDatabase db = getDatabase();
        SQLiteStatement statement = db.compileStatement(schema.getInsertStatement());
        return new Inserter<>(schema, statement);
    }

    public <T> long insert(Schema<T> schema, T model) {
        Inserter<T> sth = prepareInsert(schema);
        return sth.execute(model);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getDatabase();
        return db.updateWithOnConflict(table, values, whereClause, whereArgs, SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    public Cursor query(String table, String[] columns, String whereClause, String[] whereArgs,
            String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = getDatabase();
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, table, columns, whereClause, groupBy, having, orderBy, limit);

        return db.rawQueryWithFactory(null, sql, whereArgs, table);
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

    public <T> T querySingle(Schema<T> schema, String[] columns, String whereClause, String[] whereArgs, String groupBy,
            String having, String orderBy) {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.query(schema.getTableName(), columns, whereClause, whereArgs, groupBy,
                having, orderBy, "1");

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
                createAllTables(db);
            }
        });
    }


    void dropAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            execSQL(db, schema.getDropTableStatement());
        }
    }

    void createAllTables(SQLiteDatabase db) {
        for (Schema<?> schema : schemas) {
            execSQL(db, schema.getCreateTableStatement());

            for (String statement : schema.getCreateIndexStatements()) {
                execSQL(db, statement);
            }
        }
    }

    private void execSQL(SQLiteDatabase db, String sql) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, sql);
        }
        db.execSQL(sql);
    }

    public List<NamedDdl> getNamedDdls() {
        List<NamedDdl> list = new ArrayList<>();

        for (Schema<?> schema : schemas) {
            NamedDdl namedDDL = new NamedDdl(schema.getTableName(),
                    schema.getCreateTableStatement(),
                    schema.getCreateIndexStatements());
            list.add(namedDDL);
        }

        return list;
    }

    // SQLiteOpenHelper

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        migration.onMigrate(db, getNamedDdls(), oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        migration.onMigrate(db, getNamedDdls(), oldVersion, newVersion);
    }
}
