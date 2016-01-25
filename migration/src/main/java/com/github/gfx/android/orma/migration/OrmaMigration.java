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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A migration engine that uses both {@link ManualStepMigration} and {@link SchemaDiffMigration}.
 * </p>
 * <p>
 * By default, this class is in auto schema version mode,
 * where {@code BuildConfig.VERSION_CODE} is used as the {@code schemaVersion} on release build,
 * or the application updated time is used as the {@code schemaVersion} on debug build.
 *</p>
 *
 * <p>
 * You can set the schema version manually by {@link OrmaMigration.Builder#schemaVersion(int)}.
 * </p>
 */
public class OrmaMigration implements MigrationEngine {

    final int version;

    final ManualStepMigration manualStepMigration;

    final SchemaDiffMigration schemaDiffMigration;

    public static Builder builder(@NonNull Context context) {
        return new Builder(context);
    }

    /**
     * To control the schema version, use this constructor.
     *
     * @param version The database schema version used in {@link android.database.sqlite.SQLiteOpenHelper}.
     * @param manualStepMigration Used to control manual-step migration
     * @param schemaDiffMigration Used to control automatic migration
     */
    protected OrmaMigration(int version,
            @NonNull ManualStepMigration manualStepMigration, @NonNull SchemaDiffMigration schemaDiffMigration) {
        this.version = version;
        this.manualStepMigration = manualStepMigration;
        this.schemaDiffMigration = schemaDiffMigration;
    }

    @Deprecated
    public OrmaMigration(@NonNull Context context, int versionForManualStepMigration, boolean trace) {
        manualStepMigration = new ManualStepMigration(versionForManualStepMigration, trace);
        schemaDiffMigration = new SchemaDiffMigration(context, trace);
        version = schemaDiffMigration.getVersion(); // based on update/install time
    }

    @Deprecated
    public OrmaMigration(@NonNull Context context, int versionForManualStepMigration) {
        this(context, versionForManualStepMigration, extractDebuggable(context));
    }

    protected static boolean extractDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)
                == ApplicationInfo.FLAG_DEBUGGABLE;
    }

    public ManualStepMigration getManualStepMigration() {
        return manualStepMigration;
    }

    public SchemaDiffMigration getSchemaDiffMigration() {
        return schemaDiffMigration;
    }

    /**
     * Delegates to {@link ManualStepMigration#addStep(int, ManualStepMigration.Step)}.
     * @param version A target version for the step
     * @param step A migration step task for {@code version}
     */
    public void addStep(int version, @NonNull ManualStepMigration.Step step) {
        manualStepMigration.addStep(version, step);
    }

    /**
     * Delegates to {@link SchemaDiffMigration#getVersion()}.
     *
     * @return The current version of the database.
     */
    @Override
    public int getVersion() {
        return version;
    }

    /**
     * Starts migration process, invoking {@link ManualStepMigration#start(SQLiteDatabase, List)} first, and then
     * invoking {@link SchemaDiffMigration#start(SQLiteDatabase, List)}.
     *
     * @param db A writable database
     * @param schemas Destination schemas
     */
    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<? extends MigrationSchema> schemas) {
        manualStepMigration.start(db, schemas);
        schemaDiffMigration.start(db, schemas);
    }

    @NonNull
    static PackageInfo getPackageInfo(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    protected static int extractRevisionByLastUpdateTime(@NonNull Context context) {
        long milliseconds = getPackageInfo(context).lastUpdateTime;
        if (milliseconds != 0) {
            return (int)TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        } else {
            return 1; // robolectric;
        }
    }

    protected static int extractRevisionByVersionCode(@NonNull  Context context) {
        int versionCode = getPackageInfo(context).versionCode;
        if (versionCode != 0) {
            return versionCode;
        } else {
            return 1; // robolectric
        }
    }

    public static class Builder {

        final Context context;

        final boolean debug;

        int schemaVersion = 0;

        int manualStepMigrationVersion = 1;

        boolean trace;

        SparseArray<ManualStepMigration.Step> steps = new SparseArray<>();

        Builder(Context context) {
            this.context = context;
            debug = extractDebuggable(context);
            trace = debug;
            autoSchemaVersion(true);
        }

        public Builder schemaVersion(@IntRange(from = 1) int version) {
            schemaVersion = version;
            return this;
        }

        public Builder autoSchemaVersion(boolean value) {
            if (value) {
                if (debug) {
                    schemaVersion = extractRevisionByLastUpdateTime(context);
                } else {
                    schemaVersion = extractRevisionByVersionCode(context);
                }
            } else {
                schemaVersion = 0;
            }
            return this;
        }

        public Builder manualStepMigrationVersion(@IntRange(from = 1) int version) {
            manualStepMigrationVersion = version;
            return this;
        }

        public Builder trace(boolean value) {
            trace = value;
            return this;
        }

        public Builder step(@IntRange(from = 1) int version, @NonNull ManualStepMigration.Step step) {
            steps.append(version, step);
            return this;
        }

        public OrmaMigration build() {
            if (schemaVersion == 0) {
                throw new IllegalArgumentException("no schemaVersion(int) nor autoSchemaVersion(boolean) is supplied");
            }

            ManualStepMigration manualStepMigration = new ManualStepMigration(manualStepMigrationVersion, steps, trace);
            SchemaDiffMigration schemaDiffMigration = new SchemaDiffMigration(context, trace);
            return new OrmaMigration(schemaVersion, manualStepMigration, schemaDiffMigration);
        }
    }
}
