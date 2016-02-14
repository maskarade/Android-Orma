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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.List;

/**
 * <p>
 * A migration engine that composes {@link ManualStepMigration} and {@link SchemaDiffMigration}.
 * </p>
 * <p>
 * By default, this class is in auto schema version mode,
 * where {@code BuildConfig.VERSION_CODE} is used as the {@code schemaVersion} on release build,
 * or the application updated time is used as the {@code schemaVersion} on debug build.
 * </p>
 *
 * <pre>Example:
 * <code>
 *      OrmaMigration.builder(context)
 *          .trace(true) // optional
 *          .versionForManualStepMigration(BuildConfig.VERSION_CODE) // optional
 *          .schemaHashForSchemaDiffMigration(OrmaDatabase.SHCEMA_HASH) // required
 *          .build()
 * </code></pre>
 */
public class OrmaMigration extends AbstractMigrationEngine {

    final ManualStepMigration manualStepMigration;

    final SchemaDiffMigration schemaDiffMigration;

    /**
     * To control the schema version, use this constructor.
     *
     * @param manualStepMigration Used to control manual-step migration
     * @param schemaDiffMigration Used to control automatic migration
     */
    protected OrmaMigration(ManualStepMigration manualStepMigration, SchemaDiffMigration schemaDiffMigration) {

        this.manualStepMigration = manualStepMigration;
        this.schemaDiffMigration = schemaDiffMigration;
    }

    public static Builder builder(@NonNull Context context) {
        return new Builder(context);
    }

    public ManualStepMigration getManualStepMigration() {
        return manualStepMigration;
    }

    public SchemaDiffMigration getSchemaDiffMigration() {
        return schemaDiffMigration;
    }

    /**
     * Use {@link Builder#step(int, ManualStepMigration.Step)} instead.
     *
     * @param version A target version for the step
     * @param step    A migration step task for {@code version}
     */
    public void addStep(int version, @NonNull ManualStepMigration.Step step) {
        manualStepMigration.addStep(version, step);
    }

    /**
     * Starts migration process, invoking {@link ManualStepMigration#start(SQLiteDatabase, List)} first, and then
     * invoking {@link SchemaDiffMigration#start(SQLiteDatabase, List)}.
     *
     * @param db      A writable database
     * @param schemas Destination schemas
     */
    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<? extends MigrationSchema> schemas) {
        manualStepMigration.start(db, schemas);
        schemaDiffMigration.start(db, schemas);
    }

    public static class Builder {

        final Context context;

        final boolean debug;

        int versionForManualStepMigration = 0;

        @Nullable
        String schemaHashForSchemaDiffMigration = null;

        boolean trace;

        SparseArray<ManualStepMigration.Step> steps = new SparseArray<>();

        Builder(Context context) {
            this.context = context;
            debug = extractDebuggable(context);
            trace = debug;
        }

        public Builder versionForManualStepMigration(@IntRange(from = 1) int version) {
            versionForManualStepMigration = version;
            return this;
        }

        public Builder schemaHashForSchemaDiffMigration(@NonNull String schemaHash) {
            schemaHashForSchemaDiffMigration = schemaHash;
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
            if (versionForManualStepMigration == 0) {
                versionForManualStepMigration = extractVersionCode(context);
            }

            if (schemaHashForSchemaDiffMigration == null) {
                throw new IllegalStateException("You must set OrmaDatabase.SCHEMA_HASH to schemaHashForSchemaDiffMigration.");
            }

            ManualStepMigration manualStepMigration = new ManualStepMigration(context, versionForManualStepMigration, steps,
                    trace);
            SchemaDiffMigration schemaDiffMigration = new SchemaDiffMigration(context, schemaHashForSchemaDiffMigration,
                    trace);
            return new OrmaMigration(manualStepMigration, schemaDiffMigration);
        }
    }
}
