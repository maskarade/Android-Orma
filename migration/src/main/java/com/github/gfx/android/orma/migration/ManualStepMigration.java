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
package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.core.Database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Version based, step-by-step migration engine with {@link Database#getVersion()},
 * which refers {@code PRAGMA user_version} in SQLite.
 * </p>
 * <p>
 * This migration engine is compatible with {@link android.database.sqlite.SQLiteOpenHelper}.
 * That is, you can migrate from {@code SQLiteOpenHelper}, or migrate to {@code SQLiteOpenHelper}.
 * </p>
 */
@SuppressLint("Assert")
public class ManualStepMigration extends AbstractMigrationEngine {

    public static final String TAG = "ManualStepMigration";

    public static final String MIGRATION_STEPS_TABLE = "orma_migration_steps";

    static final String kId = "id";

    static final String kVersion = "version";

    static final String kSql = "sql";

    public static final String MIGRATION_STEPS_DDL = "CREATE TABLE IF NOT EXISTS "
            + MIGRATION_STEPS_TABLE + " ("
            + kId + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + kVersion + " INTEGER NOT NULL, "
            + kSql + " TEXT NULL, "
            + "created_timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)";

    final String versionName; // TODO: save it to MIGRATION_STEPS_TABLE

    final int versionCode; // TODO: save it to MIGRATION_STEPS_TABLE

    final int version;

    final SparseArray<Step> steps;

    boolean tableCreated = false;

    public ManualStepMigration(@NonNull Context context, int version, @NonNull SparseArray<Step> steps, @NonNull TraceListener traceListener) {
        super(traceListener);
        this.versionName = extractVersionName(context);
        this.versionCode = extractVersionCode(context);
        this.version = version;
        this.steps = steps.clone();
    }

    public ManualStepMigration(@NonNull Context context, int version, @NonNull TraceListener traceListener) {
        this(context, version, new SparseArray<Step>(0), traceListener);
    }

    public ManualStepMigration(@NonNull Context context, int version, boolean trace) {
        this(context, version, new SparseArray<Step>(0), trace ? TraceListener.LOGCAT : TraceListener.EMPTY);
    }

    @NonNull
    @Override
    public String getTag() {
        return TAG;
    }

    public void addStep(int version, @NonNull Step step) {
        steps.put(version, step);
    }

    public int fetchDbVersion(@NonNull  Database db) {
        return db.getVersion();
    }

    @Override
    public void start(@NonNull Database db, @NonNull List<? extends MigrationSchema> schemas) {
        int dbVersion = fetchDbVersion(db);

        if (dbVersion == 0) {
            db.setVersion(version);
            return;
        }

        if (dbVersion == version) {
            return;
        }

        trace("start migration from %d to %d", dbVersion, version);

        if (dbVersion < version) {
            upgrade(db, dbVersion, version);
        } else {
            downgrade(db, dbVersion, version);
        }
    }

    void ensureHistoryTableExists(@NonNull Database db) {
        if (!tableCreated) {
            db.execSQL(MIGRATION_STEPS_DDL);
            tableCreated = true;
        }
    }

    public void upgrade(@NonNull Database db, int oldVersion, int newVersion) {
        assert oldVersion < newVersion;

        ensureHistoryTableExists(db);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0, size = steps.size(); i < size; i++) {
            final int version = steps.keyAt(i);
            if (oldVersion < version && version <= newVersion) {
                final Step step = steps.valueAt(i);
                final Helper helper = new Helper(db, version, true);
                tasks.add(new Runnable() {
                    @Override
                    public void run() {
                        trace("%s step #%d", "upgrade", version);
                        step.up(helper);
                    }
                });
            }
        }
        runTasksInTransaction(db, tasks);
    }

    public void downgrade(@NonNull Database db, int oldVersion, int newVersion) {
        assert oldVersion > newVersion;

        ensureHistoryTableExists(db);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = steps.size() - 1; i >= 0; i--) {
            final int version = steps.keyAt(i);
            if (newVersion < version && version <= oldVersion) {
                final Step step = steps.valueAt(i);
                final Helper helper = new Helper(db, version, false);
                tasks.add(new Runnable() {
                    @Override
                    public void run() {
                        trace("%s step #%d", "downgrade", version);
                        step.down(helper);
                    }
                });
            }
        }
        runTasksInTransaction(db, tasks);
    }

    private void runTasksInTransaction(@NonNull Database db, @NonNull final List<Runnable> tasks) {
        if (tasks.isEmpty()) {
            saveStep(db, version, null);
            return;
        }

        transaction(db, new Runnable() {
            @Override
            public void run() {
                for (Runnable task : tasks) {
                    task.run();
                }
            }
        });
    }

    public void saveStep(@NonNull Database db, int version, @Nullable String sql) {
        ensureHistoryTableExists(db);

        ContentValues values = new ContentValues();
        values.put(kVersion, version);
        values.put(kSql, sql);
        db.insertOrThrow(MIGRATION_STEPS_TABLE, null, values);
        db.setVersion(version);
    }

    public void execStep(@NonNull Database db, int version, @Nullable String sql) {
        if (sql != null) {
            trace("%s", sql);
        }
        db.execSQL(sql);
        saveStep(db, version, sql);
    }

    /**
     * A migration step which handles {@code down()}, and {@code change()}.
     */
    public interface Step {

        void up(@NonNull ManualStepMigration.Helper helper);

        void down(@NonNull ManualStepMigration.Helper helper);

    }

    /**
     * A migration step which handles {@code change()}.
     */
    public static abstract class ChangeStep implements Step {

        @Override
        public void up(@NonNull ManualStepMigration.Helper helper) {
            change(helper);
        }

        @Override
        public void down(@NonNull ManualStepMigration.Helper helper) {
            change(helper);
        }

        public abstract void change(@NonNull ManualStepMigration.Helper helper);
    }

    public class Helper {

        public final int version;

        public final boolean upgrade;

        private final Database db;

        private final SqliteDdlBuilder util = new SqliteDdlBuilder();

        public Helper(Database db, int version, boolean upgrade) {
            this.db = db;
            this.version = version;
            this.upgrade = upgrade;
        }

        public void renameTable(@NonNull String fromTableName, @NonNull String toTableName) {
            String statement;
            if (upgrade) {
                statement = util.buildRenameTable(fromTableName, toTableName);
            } else {
                statement = util.buildRenameTable(toTableName, fromTableName);
            }
            execSQL(statement);
        }

        public void renameColumn(@NonNull String tableName, @NonNull String fromColumnName, @NonNull String toColumnName) {
            MigrationSchema schema = SQLiteMaster.findByTableName(db, tableName);

            List<String> statements;
            if (upgrade) {
                statements = util.buildRenameColumn(schema.getCreateTableStatement(), fromColumnName, toColumnName);
            } else {
                statements = util.buildRenameColumn(schema.getCreateTableStatement(), toColumnName, fromColumnName);
            }
            // Don't re-create index. SchemaDiffMigration will create them, anyway.

            for (String sql : statements) {
                execSQL(sql);
            }
        }

        public void execSQL(@NonNull String sql) {
            execStep(db, upgrade ? version : version - 1, sql);
        }
    }
}
