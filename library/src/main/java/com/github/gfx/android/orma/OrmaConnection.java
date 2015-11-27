package com.github.gfx.android.orma;

import com.github.gfx.android.orma.adapter.TypeAdapter;
import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.exception.DatabaseAccessOnMainThreadException;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.NamedDdl;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class OrmaConnection extends SQLiteOpenHelper {

    static final String TAG = OrmaConnection.class.getSimpleName();

    static final String[] countSelections = {"COUNT(*)"};

    final List<Schema<?>> schemas;

    final MigrationEngine migration;

    final boolean wal;

    final boolean trace;

    final TypeAdapterRegistry typeAdapters = new TypeAdapterRegistry();

    final AccessThreadConstraint readOnMainThread;

    final AccessThreadConstraint writeOnMainThread;

    public OrmaConnection(@NonNull OrmaConfiguration configuration, List<Schema<?>> schemas) {
        super(configuration.context, configuration.name, null, configuration.migrationEngine.getVersion());
        this.schemas = schemas;
        this.migration = configuration.migrationEngine;
        this.wal = configuration.wal;

        this.trace = configuration.debug;
        this.readOnMainThread = configuration.readOnMainThread;
        this.writeOnMainThread = configuration.readOnMainThread;

        if (wal) {
            enableWal();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void enableWal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setWriteAheadLoggingEnabled(true);
        }
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

    public TypeAdapterRegistry getTypeAdapters() {
        return typeAdapters;
    }

    public void addTypeAdapters(TypeAdapter<?>... adapters) {
        for (TypeAdapter<?> typeAdapter : adapters) {
            typeAdapters.add(typeAdapter);
        }
    }

    public <T> T createModel(Schema<T> schema, ModelBuilder<T> builder) {
        long id = insert(schema, builder.build());
        ColumnDef<?> primaryKey = schema.getPrimaryKey();
        String whereClause = '"' + primaryKey.name + '"' + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return querySingle(schema, schema.getEscapedColumnNames(), whereClause, whereArgs, null, null, null);
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
        return db.updateWithOnConflict(schema.getTableName(), values, whereClause, whereArgs,
                SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    public Cursor query(Schema<?> schema, String[] columns, String whereClause, String[] whereArgs,
            String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = SQLiteQueryBuilder.buildQueryString(
                false, schema.getTableName(), columns, whereClause, groupBy, having, orderBy, limit);
        trace(sql);
        return db.rawQueryWithFactory(null, sql, whereArgs, schema.getTableName());
    }

    public int count(Schema<?> schema, String whereClause, String[] whereArgs) {
        Cursor cursor = query(schema, countSelections, whereClause, whereArgs, null, null, null, null);
        try {
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }

    public <T> T querySingle(Schema<T> schema, String[] columns, String whereClause, String[] whereArgs, String groupBy,
            String having, String orderBy) {
        Cursor cursor = query(schema, columns, whereClause, whereArgs, groupBy, having, orderBy, "1");

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
        return db.delete(schema.getTableName(), whereClause, whereArgs);
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

    private void trace(String sql) {
        if (trace) {
            Log.v(TAG, sql);
        }
    }

    // SQLiteOpenHelper

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (wal) {
            db.enableWriteAheadLogging();
        }
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
