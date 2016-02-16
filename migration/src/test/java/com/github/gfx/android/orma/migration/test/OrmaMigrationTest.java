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
package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.OrmaMigration;
import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.migration.test.util.SchemaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class OrmaMigrationTest {

    static String SCHEMA_HASH = "aaa";

    static int VERSION = 100;

    SQLiteDatabase db;

    OrmaMigration migration;

    static Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);
        db.setVersion(1);

        migration = OrmaMigration.builder(getContext())
                .schemaHashForSchemaDiffMigration(SCHEMA_HASH)
                .versionForManualStepMigration(VERSION)
                .trace(true)
                .build();

        migration.getManualStepMigration()
                .execStep(db, 1, "CREATE TABLE dummy (id INTEGER PRIMARY KEY)");
    }

    @Test
    public void testBuilder() throws Exception {
        migration = OrmaMigration.builder(getContext())
                .schemaHashForSchemaDiffMigration(SCHEMA_HASH)
                .versionForManualStepMigration(VERSION)
                .step(2, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        helper.execSQL("CREATE TABLE step_2 (id INTEGER PRIMARY KEY)");
                    }
                })
                .step(4, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        helper.execSQL("CREATE TABLE step_4 (id INTEGER PRIMARY KEY)");
                    }
                })
                .step(8, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        helper.execSQL("CREATE TABLE step_8 (id INTEGER PRIMARY KEY)");
                    }
                })
                .step(16, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        helper.execSQL("CREATE TABLE step_16 (id INTEGER PRIMARY KEY)");
                    }
                })
                .build();

        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());

        Map<String, SQLiteMaster> tables = SQLiteMaster.loadTables(db);
        assertThat(tables.containsKey("step_2"), is(true));
        assertThat(tables.containsKey("step_4"), is(true));
        assertThat(tables.containsKey("step_8"), is(true));
        assertThat(tables.containsKey("step_16"), is(true));
    }

    @Test
    public void testHomeostasis() throws Exception {
        migration.addStep(2, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_2 (id INTEGER PRIMARY KEY)");
            }
        });
        migration.addStep(4, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_4 (id INTEGER PRIMARY KEY)");
            }
        });
        migration.addStep(8, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_8 (id INTEGER PRIMARY KEY)");
            }
        });

        migration.addStep(16, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_16 (id INTEGER PRIMARY KEY)");
            }
        });

        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());

        Map<String, SQLiteMaster> tables = SQLiteMaster.loadTables(db);
        assertThat(tables.containsKey("step_2"), is(true));
        assertThat(tables.containsKey("step_4"), is(true));
        assertThat(tables.containsKey("step_8"), is(true));
        assertThat(tables.containsKey("step_16"), is(true));
    }

    @Test
    public void testForceRecreate() throws Exception {
        // initialize
        String[] initData = {
                "CREATE TABLE foo (id PRIMARY KEY)",
                "CREATE TABLE bar (id PRIMARY KEY)",

                "INSERT INTO foo (id) VALUES (1)",
                "INSERT INTO bar (id) VALUES (2)",
        };

        for (String s : initData) {
            migration.getManualStepMigration().execStep(db, 2, s);
        }

        // define steps
        migration.addStep(3, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("DROP TABLE foo");
                helper.execSQL("DROP TABLE bar");
            }
        });

        List<SchemaData> schemas = Arrays.asList(
                new SchemaData("foo", "CREATE TABLE foo (id PRIMARY KEY, field01 TEXT NOT NULL, field02 TEXT NOT NULL)"),
                new SchemaData("bar", "CREATE TABLE bar (id PRIMARY KEY, field10 TEXT NOT NULL, field20 TEXT NOT NULL)")
        );

        // start migration
        migration.start(db, schemas);

        Map<String, SQLiteMaster> tables = SQLiteMaster.loadTables(db);
        assertThat(tables, hasKey("foo"));
        assertThat(tables, hasKey("bar"));
        assertThat(tables.get("foo").sql, is(schemas.get(0).getCreateTableStatement()));
        assertThat(tables.get("bar").sql, is(schemas.get(1).getCreateTableStatement()));
    }
}
