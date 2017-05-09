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

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A MigrationEngine to drop and create all the tables.
 * This is intended to be used in development.
 */
public class ResetMigration extends AbstractMigrationEngine {

    public static final String TAG = "ResetMigration";

    public ResetMigration(@NonNull TraceListener traceListener) {
        super(traceListener);
    }

    @NonNull
    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void start(@NonNull final SQLiteDatabase db, @NonNull List<? extends MigrationSchema> schemas) {
        final List<String> statements = new ArrayList<>();

        for (MigrationSchema schema : SQLiteMaster.loadTables(db).values()) {
            statements.add("DROP TABLE " + SqliteDdlBuilder.ensureEscaped(schema.getTableName()));
        }

        for (MigrationSchema schema : schemas) {
            statements.add(schema.getCreateTableStatement());
            statements.addAll(schema.getCreateIndexStatements());
        }

        transaction(db, new Runnable() {
            @Override
            public void run() {
                for (String statement : statements) {
                    trace("%s", statement);
                    db.execSQL(statement);
                }
            }
        });
    }
}
