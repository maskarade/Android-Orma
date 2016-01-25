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

import com.github.gfx.android.orma.adapter.TypeAdapter;
import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.exception.DatabaseAccessOnMainThreadException;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteParserUtils;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Low-level interface to Orma database connection.
 */
public class OrmaConnection extends SQLiteOpenHelper {

    static final String TAG = "Orma";

    static final String[] countSelections = {"COUNT(*)"};

    final List<Schema<?>> schemas;

    final MigrationEngine migration;

    final boolean wal;

    final boolean foreignKeys;

    final boolean tryParsingSql;

    final boolean trace;

    final TypeAdapterRegistry typeAdapterRegistry;

    final AccessThreadConstraint readOnMainThread;

    final AccessThreadConstraint writeOnMainThread;

    public OrmaConnection(@NonNull OrmaConfiguration<?> configuration, List<Schema<?>> schemas) {
        super(configuration.context, configuration.name, null, configuration.migrationEngine.getVersion());
        this.schemas = schemas;
        this.migration = configuration.migrationEngine;
        this.foreignKeys = configuration.foreignKeys;
        this.wal = configuration.wal;
        this.typeAdapterRegistry = configuration.typeAdapterRegistry;

        this.tryParsingSql = configuration.tryParsingSql;
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

    @NonNull
    public <SourceType> TypeAdapter<SourceType> getTypeAdapter(Type sourceType) {
        return typeAdapterRegistry.get(sourceType);
    }

    @Deprecated // because type adapter registry will become global, static object
    public TypeAdapterRegistry getTypeAdapterRegistry() {
        return typeAdapterRegistry;
    }

    public <T> T createModel(Schema<T> schema, ModelFactory<T> factory) {
        Inserter<T> sth = new Inserter<>(this, schema, schema.getInsertStatement(OnConflict.NONE));
        long id = sth.execute(factory.call());

        ColumnDef<T, ?> primaryKey = schema.getPrimaryKey();
        String whereClause = '"' + primaryKey.name + '"' + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return querySingle(schema, schema.getEscapedColumnNames(), whereClause, whereArgs, null, null, null, 0);
    }

    public int update(Schema<?> schema, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(schema.getEscapedTableName(), values, whereClause, whereArgs);
    }

    @NonNull
    public Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs) {
        trace(sql, bindArgs);
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, bindArgs);
    }

    public long rawQueryForLong(@NonNull String sql, @NonNull String... bindArgs) {
        trace(sql, bindArgs);
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
        SQLiteCursor cursor = (SQLiteCursor) query(schema, columns, whereClause, whereArgs, groupBy, having, orderBy,
                offset + ",1");

        try {
            if (cursor.moveToFirst()) {
                return schema.newModelFromCursor(this, cursor);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    public int delete(@NonNull Schema<?> schema, @Nullable String whereClause, @Nullable String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "DELETE FROM " + schema.getEscapedTableName()
                + (!TextUtils.isEmpty(whereClause) ? " WHERE " + whereClause : "");
        trace(sql, whereArgs);
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindAllArgsAsStrings(whereArgs);
        try {
            return statement.executeUpdateDelete();
        } finally {
            statement.close();
        }
    }

    public void transactionNonExclusiveSync(@NonNull TransactionTask task) {
        SQLiteDatabase db = getReadableDatabase();
        trace("begin transaction (non exclusive)", null);
        db.beginTransactionNonExclusive();

        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            task.onError(e);
        } finally {
            db.endTransaction();
            trace("end transaction (non exclusive)", null);
        }
    }

    public void transactionNonExclusiveAsync(@NonNull final TransactionTask task) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                transactionNonExclusiveSync(task);
            }
        });
    }

    @WorkerThread
    public void transactionSync(@NonNull TransactionTask task) {
        SQLiteDatabase db = getWritableDatabase();
        trace("begin transaction", null);
        db.beginTransaction();

        try {
            task.execute();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            task.onError(e);
        } finally {
            db.endTransaction();
            trace("end transaction", null);
        }
    }

    public void transactionAsync(@NonNull final TransactionTask task) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
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

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
        trace(sql, bindArgs);
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
            if (tryParsingSql) {
                SQLiteParserUtils.parse(schema.getCreateTableStatement());
            }
            execSQL(db, schema.getCreateTableStatement());

            for (String statement : schema.getCreateIndexStatements()) {
                execSQL(db, statement);
            }
        }
    }

    void execSQL(SQLiteDatabase db, String sql) {
        trace(sql, null);
        db.execSQL(sql);
    }

    void trace(@NonNull String sql, @Nullable Object[] bindArgs) {
        if (trace) {
            String prefix = "[" + Thread.currentThread().getName() + "] ";
            if (bindArgs == null) {
                Log.v(TAG, prefix + sql);
            } else {
                Log.v(TAG, prefix + sql + " - " + Arrays.deepToString(bindArgs));
            }
        }
    }

    // SQLiteOpenHelper

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        if (wal && getDatabaseName() != null && !isRunningOnJellyBean()) {
            db.enableWriteAheadLogging();
        }
        if (foreignKeys) {
            db.execSQL("PRAGMA foreign_keys = ON");
        } else {
            db.execSQL("PRAGMA foreign_keys = OFF");
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
