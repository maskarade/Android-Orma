package com.github.gfx.android.orma.migration;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;

@SuppressLint("Assert")
public class ManualStepMigration implements MigrationEngine {

    public static final String TAG = "ManualStepMigration";

    static final String MIGRATION_HISTORY_NAME = "orma_manual_migrations";

    static final String MIGRATION_HISTORY_DDL = "CREATE TABLE IF NOT EXISTS "
            + MIGRATION_HISTORY_NAME + " (version INTEGER NOT NULL, sql TEXT NULL, created_time_millis INTEGER NOT NULL)";

    static final String kVersion = "version";

    static final String kSql = "sql";

    static final String kCreatedTimeMillis = "created_time_millis";

    final int version;

    final boolean trace;

    final SparseArray<Step> steps = new SparseArray<>(0);

    public ManualStepMigration(@NonNull Context context, boolean trace) {
        this(extractVersion(context), trace);
    }

    public ManualStepMigration(int version, boolean trace) {
        this.version = version;
        this.trace = trace;
    }

    static int extractVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        int version;
        try {
            version = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).versionCode;
            if (version == 0) {
                version = 1; // robolectric
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
        return version;
    }

    public void addStep(int newVersion, @NonNull ManualStepMigration.Step step) {
        steps.put(newVersion, step);
    }

    @Override
    public int getVersion() {
        return version;
    }

    public int getDbVersion(SQLiteDatabase db) {
        Cursor cursor = db.query(MIGRATION_HISTORY_NAME, new String[]{kVersion},
                null, null, null, null, kCreatedTimeMillis + " DESC", "1");
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<NamedDdl> schemas) {
        createMigrationHistoryTable(db);

        int oldVersion = getDbVersion(db);

        if (oldVersion == 0) {
            Log.v(TAG, "Skip migration because there is no manual migration history");
            return;
        }

        if (oldVersion == version) {
            Log.v(TAG, "No need to run migration");
        }

        if (oldVersion < version) {
            upgrade(db, oldVersion, version);
        } else {
            downgrade(db, oldVersion, version);
        }
    }

    public void createMigrationHistoryTable(SQLiteDatabase db) {
        db.execSQL(MIGRATION_HISTORY_DDL);
    }

    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        assert oldVersion < newVersion;

        for (int i = 0, size = steps.size(); i < size; i++) {
            int version = steps.keyAt(i);
            if (oldVersion < version && version <= newVersion) {
                if (trace) {
                    Log.v(TAG, "upgrade step #" + version + " from " + oldVersion + " to " + newVersion);
                }
                Step step = steps.valueAt(i);
                step.run(new Helper(db, version, true));
            }
        }
    }

    public void downgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        assert oldVersion > newVersion;

        for (int i = steps.size() - 1; i >= 0; i--) {
            int version = steps.keyAt(i);
            if (newVersion < version && version <= oldVersion) {
                if (trace) {
                    Log.v(TAG, "downgrade step #" + version + " from " + oldVersion + " to " + newVersion);
                }
                Step step = steps.valueAt(i);
                step.run(new Helper(db, version, false));
            }
        }
    }

    public void imprintStep(SQLiteDatabase db, int version, @Nullable String sql) {
        ContentValues values = new ContentValues();
        values.put(kVersion, version);
        values.put(kSql, sql);
        values.put(kCreatedTimeMillis, System.currentTimeMillis());

        db.insertWithOnConflict(MIGRATION_HISTORY_NAME, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    public interface Step {

        void run(@NonNull ManualStepMigration.Helper helper);
    }

    public class Helper {

        public final int version;

        public final boolean upgrade;

        final SQLiteDatabase db;

        final SqliteDdlBuilder sqliteDdlBuilder = new SqliteDdlBuilder();

        public Helper(SQLiteDatabase db, int version, boolean upgrade) {
            this.db = db;
            this.version = version;
            this.upgrade = upgrade;
        }

        public void renameTable(@NonNull String fromTableName, @NonNull String toTableName) {
            if (upgrade) {
                String sql = sqliteDdlBuilder.buildRenameTable(fromTableName, toTableName);
                execSQL(sql);
            } else {
                String sql = sqliteDdlBuilder.buildRenameTable(toTableName, fromTableName);
                execSQL(sql);
            }
        }

        public void execSQL(@NonNull String sql) {
            db.execSQL(sql);
            imprintStep(db, upgrade ? version  : version - 1, sql);
        }
    }
}
