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

import com.github.gfx.android.orma.exception.DatabaseAccessOnMainThreadException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.sqliteparser.SQLiteParserUtils;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

/**
 * Low-level interface to Orma database connection.
 */
public class OrmaConnection {

    static final String TAG = "Orma";

    final String name;

    /**
     * Do not use "db" field directly. Use `getWritableDatabase()` or `getReadableDatabase()` instead.
     */
    final SQLiteDatabase db;

    final List<Schema<?>> schemas;

    final MigrationEngine migration;

    final boolean wal;

    final boolean foreignKeys;

    final boolean tryParsingSql;

    final boolean trace;

    final AccessThreadConstraint readOnMainThread;

    final AccessThreadConstraint writeOnMainThread;

    final Map<Observer<Relation<?, ?>>, Relation<?, ?>> observerMap = new WeakHashMap<>();

    boolean migrationCompleted = false;

    public OrmaConnection(@NonNull OrmaDatabaseBuilderBase<?> builder, List<Schema<?>> schemas) {
        this.name = builder.name;

        this.schemas = schemas;
        this.migration = builder.migrationEngine;
        this.foreignKeys = builder.foreignKeys;
        this.wal = builder.wal;

        this.tryParsingSql = builder.tryParsingSql;
        this.trace = builder.trace;
        this.readOnMainThread = builder.readOnMainThread;
        this.writeOnMainThread = builder.writeOnMainThread;
        this.db = openDatabase(builder.context);

        checkSchemas(schemas);
    }

    private SQLiteDatabase openDatabase(Context context) {
        SQLiteDatabase db;
        if (name == null) {
            db = SQLiteDatabase.create(null);
        } else {
            db = context.openOrCreateDatabase(name, openFlags(), null, null);
        }
        onConfigure(db);
        return db;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int openFlags() {
        if (wal && isRunningOnJellyBean()) {
            return Context.MODE_ENABLE_WRITE_AHEAD_LOGGING;
        } else {
            return 0;
        }
    }

    private boolean isRunningOnJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    @Nullable
    public String getDatabaseName() {
        return name;
    }

    @NonNull
    public List<Schema<?>> getSchemas() {
        return schemas;
    }

    public synchronized SQLiteDatabase getWritableDatabase() {
        if (writeOnMainThread != AccessThreadConstraint.NONE) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                if (writeOnMainThread == AccessThreadConstraint.FATAL) {
                    throw new DatabaseAccessOnMainThreadException("Writing things must run in background");
                } else {
                    Log.w(TAG, "Writing things must run in background");
                }
            }
        }
        if (!migrationCompleted) {
            onMigrate(db);
            migrationCompleted = true;
        }
        return db;
    }

    public synchronized SQLiteDatabase getReadableDatabase() {
        if (readOnMainThread != AccessThreadConstraint.NONE) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                if (readOnMainThread == AccessThreadConstraint.FATAL) {
                    throw new DatabaseAccessOnMainThreadException("Reading things must run in background");
                } else {
                    Log.w(TAG, "Reading things must run in background");
                }
            }
        }
        if (!migrationCompleted) {
            onMigrate(db);
            migrationCompleted = true;
        }
        return db;
    }

    @NonNull
    public <T> T createModel(Schema<T> schema, ModelFactory<T> factory) {
        T model = factory.call();
        Inserter<T> sth = new Inserter<>(this, schema);
        long id = sth.execute(model);

        ColumnDef<T, ?> primaryKey = schema.getPrimaryKey();
        String whereClause = primaryKey.getQualifiedName() + " = ?";
        String primaryKeyValue;
        if (primaryKey.isAutoValue()) {
            primaryKeyValue = Long.toString(id);
        } else {
            primaryKeyValue = String.valueOf(primaryKey.getSerialized(model));
        }
        String[] whereArgs = {primaryKeyValue};
        T createdModel = querySingle(schema, schema.getDefaultResultColumns(), whereClause, whereArgs, null, null, null, 0);
        if (createdModel == null) {
            throw new NoValueException("Can't retrieve the created model for " + model + " (rowid=" + id + ")");
        }
        return createdModel;
    }

    public int update(Schema<?> schema, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        if (trace) {
            traceUpdateQuery(schema, values, whereClause, whereArgs);
        }
        return db.update(schema.getEscapedTableName(), values, whereClause, whereArgs);
    }

    private void traceUpdateQuery(Schema<?> schema, ContentValues values, String whereClause, String[] whereArgs) {
        // copied from SQLiteDatabase#updateWithOnConflict()
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(schema.getEscapedTableName());
        sql.append(" SET ");

        // move all bind args to one array
        int setValuesSize = values.size();
        int bindArgsSize = (whereArgs == null) ? setValuesSize : (setValuesSize + whereArgs.length);
        Object[] bindArgs = new Object[bindArgsSize];
        int i = 0;
        for (String colName : values.keySet()) {
            sql.append((i > 0) ? "," : "");
            sql.append(colName);
            bindArgs[i++] = values.get(colName);
            sql.append("=?");
        }
        if (whereArgs != null) {
            for (i = setValuesSize; i < bindArgsSize; i++) {
                bindArgs[i] = whereArgs[i - setValuesSize];
            }
        }
        if (!TextUtils.isEmpty(whereClause)) {
            sql.append(" WHERE ");
            sql.append(whereClause);
        }

        trace(sql.toString(), bindArgs);
    }

    @NonNull
    public Cursor rawQuery(@NonNull String sql, String... bindArgs) {
        trace(sql, bindArgs);
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(sql, bindArgs);
    }

    public long rawQueryForLong(@NonNull String sql, String... bindArgs) {
        trace(sql, bindArgs);
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.longForQuery(db, sql, bindArgs);
    }

    @NonNull
    public Cursor query(Schema<?> schema, String[] columns, String whereClause, String[] bindArgs,
            String groupBy, String having, String orderBy, String limit) {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, schema.getSelectFromTableClause(), columns, whereClause, groupBy, having, orderBy, limit);
        return rawQuery(sql, bindArgs);
    }

    @Nullable
    public <T> T querySingle(Schema<T> schema, String[] columns, String whereClause, String[] whereArgs, String groupBy,
            String having, String orderBy, long offset) {
        SQLiteCursor cursor = (SQLiteCursor) query(schema, columns, whereClause, whereArgs, groupBy, having, orderBy,
                offset + ",1");

        try {
            if (cursor.moveToFirst()) {
                return schema.newModelFromCursor(this, cursor, 0);
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

    public void transactionNonExclusiveSync(@NonNull Runnable task) {
        SQLiteDatabase db = getReadableDatabase();
        trace("begin transaction (non exclusive)", null);
        db.beginTransactionNonExclusive();

        try {
            task.run();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            trace("end transaction (non exclusive)", null);
        }
    }

    @WorkerThread
    public void transactionSync(@NonNull Runnable task) {
        SQLiteDatabase db = getWritableDatabase();
        trace("begin transaction", null);
        db.beginTransaction();
        try {
            task.run();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            trace("end transaction", null);
        }
    }

    /**
     * Deletes all the columns in all the tables provided for testing.
     */
    public void deleteAll() {
        transactionSync(new Runnable() {
            @Override
            public void run() {
                for (Schema<?> schema : schemas) {
                    delete(schema, null, null);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <R extends Relation<?, ?>> Observable<R> createQueryObservable(R relation) {
        PublishSubject<R> subject = PublishSubject.create();
        observerMap.put((PublishSubject<Relation<?, ?>>) subject, relation);
        return subject;
    }


    public void trigger(Schema<?> schema) {
        if (!observerMap.isEmpty()) {
            for (Map.Entry<Observer<Relation<?, ?>>, Relation<?, ?>> entry : observerMap.entrySet()) {
                if (schema == entry.getValue().getSchema()) {
                    entry.getKey().onNext(entry.getValue());
                }
            }
        }
    }

    public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
        trace(sql, bindArgs);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql, bindArgs);
    }

    protected void checkSchemas(List<Schema<?>> schemas) {
        if (tryParsingSql) {
            for (Schema<?> schema : schemas) {
                SQLiteParserUtils.parse(schema.getCreateTableStatement());
            }
        }
    }

    protected void execSQL(@NonNull SQLiteDatabase db, @NonNull String sql) {
        trace(sql, null);
        db.execSQL(sql);
    }

    protected void trace(@NonNull String sql, @Nullable Object[] bindArgs) {
        if (trace) {
            String prefix = "[" + Thread.currentThread().getName() + "] ";
            if (bindArgs == null) {
                Log.v(TAG, prefix + sql);
            } else {
                Log.v(TAG, prefix + sql + " - " + Arrays.deepToString(bindArgs));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void setForeignKeyConstraintsEnabled(SQLiteDatabase db, boolean enabled) {
        if (isRunningOnJellyBean()) {
            db.setForeignKeyConstraintsEnabled(enabled);
        } else {
            if (enabled) {
                execSQL(db, "PRAGMA foreign_keys = ON");
            } else {
                execSQL(db, "PRAGMA foreign_keys = OFF");
            }
        }
    }

    protected void onConfigure(SQLiteDatabase db) {
        if (wal && name != null && !isRunningOnJellyBean()) {
            db.enableWriteAheadLogging();
        }

        setForeignKeyConstraintsEnabled(db, foreignKeys);
    }

    protected void onMigrate(SQLiteDatabase db) {
        long t0 = 0;
        if (trace) {
            Log.i(TAG, "migration started");
            t0 = System.currentTimeMillis();
        }

        migration.start(db, schemas);

        if (trace) {
            Log.i(TAG, "migration finished in " + (System.currentTimeMillis() - t0) + "ms");
        }
    }
}
