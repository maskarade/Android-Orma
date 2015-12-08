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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * A migration engine that uses both {@link ManualStepMigration} and {@link SchemaDiffMigration}.
 */
public class OrmaMigration implements MigrationEngine {

    final ManualStepMigration manualStepMigration;

    final SchemaDiffMigration schemaDiffMigration;

    public OrmaMigration(@NonNull Context context, int version, boolean trace) {
        manualStepMigration = new ManualStepMigration(version, trace);
        schemaDiffMigration = new SchemaDiffMigration(context, trace);
    }

    public OrmaMigration(@NonNull Context context, int version) {
        this(context, version, extractDebuggable(context));
    }

    static boolean extractDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)
                == ApplicationInfo.FLAG_DEBUGGABLE;
    }

    public void addStep(int newVersion, @NonNull ManualStepMigration.Step step) {
        manualStepMigration.addStep(newVersion, step);
    }

    @Override
    public int getVersion() {
        return schemaDiffMigration.getVersion();
    }

    @Override
    public void start(@NonNull SQLiteDatabase db, @NonNull List<? extends MigrationSchema> schemas) {
        manualStepMigration.start(db, schemas);
        schemaDiffMigration.start(db, schemas);
    }
}
