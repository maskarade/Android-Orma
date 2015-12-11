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
package com.github.gfx.android.orma;

import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.exception.DatabaseAccessOnMainThreadException;
import com.github.gfx.android.orma.migration.MigrationEngine;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Low-level interface to Orma database connection.
 */
public class OrmaConnection extends SQLiteOpenHelper {

    static final String TAG = OrmaConnection.class.getSimpleName();

    static final String[] countSelections = {"COUNT(*)"};

    final List<Schema<?>> schemas;

    final MigrationEngine migration;

    final boolean wal;

    final boolean trace;

    final TypeAdapterRegistry typeAdapterRegistry;

    final AccessThreadConstraint readOnMainThread;

    final AccessThreadConstraint writeOnMainThread;

    public OrmaConnection(@NonNull OrmaConfiguration<?> configuration, List<Schema<?>> schemas) {
        super(configuration.context, configuration.name, null, configuration.migrationEngine.getVersion());
        this.schemas = schemas;
        this.migration = configuration.migrationEngine;
        this.wal = configuration.wal;
        this.typeAdapterRegistry = configuration.typeAdapterRegistry;

        this.trace = configuration.trace;
        this.readOnMainThread = configuration.readOnMainThread;
        this.writeOnMainThread = configuration.readOnMainThread;

        if (wal) {
            enableWal();
        }
    }

    private boolean isRunningOnJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void enableWal() {
        if (isRunningOnJellyBean()) {
            setWriteAheadLoggingEnabled(true);
        }
    }

    @NonNull
    public List<Schema<?>> getSchemas() {
        return schemas;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (writeOnMainThread != AccessThreadConstraint.NONE) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                if (writeOnMainThread == AccessThreadConstraint.FATAL) {
                    throw new DatabaseAccessOnMainThreadException("Writing things must run in background");
                } else {
                    Log.w(TAG, "Writing things must run in background");
                }
            }
        }
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (readOnMainThread != AccessThreadConstraint.NONE) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                if (readOnMainThread == AccessThreadConstraint.FATAL) {
                    throw new DatabaseAccessOnMainThreadException("Reading things must run in background");
                } else {
                    Log.w(TAG, "Reading things must run in background");
                }
            }
        }
        return super.getReadableDatabase();
    }

    public TypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }

    public <T> T createModel(Schema<T> schema, ModelFactory<T> builder) {
        long id = insert(schema, builder.create());
        ColumnDef<?> primaryKey = schema.getPrimaryKey();
        String whereClause = '"' + primaryKey.name + '"' + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return querySingle(schema, schema.getEscapedColumnNames(), whereClause, whereArgs, null, null, null, 0);
    }

    public <T> Inserter<T> prepareInsert(Schema<T> schema) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(schema.getInsertStatement());
        return new Inserter<>(this, schema, statement);
    }

    public <T> long insert(Schema<T> schema, T model) {
        Inserter<T> sth = prepareInsert(schema);
        return sth.execute(model);
    }

    public int update(Schema<?> schema, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.updateWithOnConflict(schema.getEscapedTableName(), values, whereClause, whereArgs,
                SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    @NonNull
    public Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs) {
        trace(sql);
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, bindArgs);
    }

    public long rawQueryForLong(@NonNull String sql, @NonNull String... bindArgs) {
        trace(sql);
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.longForQuery(db, sql, bindArgs);
    }

    public Cursor query(Schema<?> schema, String[] columns, String whereClause, String[] bindArgs,
            String groupBy, String having, String orderBy, String limit) {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, schema.getEscapedTableName(), columns, whereClause, groupBy, having, orderBy, limit);
        return rawQuery(sql, bindArgs);
    }

    public int count(Schema<?> schema, String whereClause, String[] whereArgs) {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, schema.getEscapedTableName(), countSelections, whereClause, null, null, null, null);
        return (int) rawQueryForLong(sql, whereArgs);
    }

    public <T> T querySingle(Schema<T> schema, String[] columns, String whereClause, String[] whereArgs, String groupBy,
            String having, String orderBy, long offset) {
        Cursor cursor = query(schema, columns, whereClause, whereArgs, groupBy, having, orderBy, offset + ",1");

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

    public int delete(@NonNull Schema<?> schema, @Nullable String whereClause, @Nullable String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(schema.getEscapedTableName(), whereClause, whereArgs);
    }

    public void transactionNonExclusiveSync(@NonNull TransactionTask task) {
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransactionNonExclusive();

        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            task.onError(e);
        } finally {
            db.endTransaction();
        }
    }

    public void transactionNonExclusiveAsync(@NonNull final TransactionTask task) {
        Schedulers.io()
                .createWorker()
                .schedule(new Action0() {
                    @Override
                    public void call() {
                        transactionNonExclusiveSync(task);
                    }
                });
    }

    public void transactionSync(@NonNull TransactionTask task) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            task.onError(e);
        } finally {
            db.endTransaction();
        }
    }

    public void transactionAsync(@NonNull final TransactionTask task) {
        Schedulers.io()
                .createWorker()
                .schedule(new Action0() {
                    @Override
                    public void call() {
                        transactionSync(task);
                    }
                });
    }

    /**
     * Drops and creates all the tables. This is provided for testing.
     */
    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            dropAllTables(db);
            createAllTables(db);

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
        trace(sql);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql, bindArgs);
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
        trace(sql);
        db.execSQL(sql);
    }

    private void trace(String sql) {
        if (trace) {
            Log.v(TAG, sql);
        }
    }

    // SQLiteOpenHelper

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        if (wal && getDatabaseName() != null && !isRunningOnJellyBean()) {
            db.enableWriteAheadLogging();
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        long t0 = System.currentTimeMillis();
        if (trace) {
            Log.v(TAG, "migration start from " + oldVersion + " to " + newVersion);
        }

        migration.start(db, schemas);

        if (trace) {
            Log.v(TAG, "migration finished in " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        long t0 = System.currentTimeMillis();
        if (trace) {
            Log.v(TAG, "migration start from " + oldVersion + " to " + newVersion);
        }

        migration.start(db, schemas);

        if (trace) {
            Log.v(TAG, "migration finished in " + (System.currentTimeMillis() - t0) + "ms");
        }
    }
}
