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
import com.github.gfx.android.orma.migration.test.util.SchemaData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ManualStepMigrationTest {

    static int VERSION = 100;

    SQLiteDatabase db;

    ManualStepMigration migration;

    List<StepContext> seq;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);

        migration = new ManualStepMigration(getContext(), VERSION, true);
        migration.saveStep(db, 1, null);

        seq = new ArrayList<>();
        setupSteps();
    }

    void setupSteps() {
        migration.addStep(2, new ManualStepMigration.Step() {
            @Override
            public void up(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_2_0 (id INTEGER PRIMARY KEY)");
                helper.execSQL("CREATE TABLE step_2_1 (id INTEGER PRIMARY KEY)");
            }

            @Override
            public void down(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("DROP TABLE step_2_0");
                helper.execSQL("DROP TABLE step_2_1");
            }
        });
        migration.addStep(4, new ManualStepMigration.Step() {
            @Override
            public void up(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_4_0 (id INTEGER PRIMARY KEY)");
                helper.execSQL("CREATE TABLE step_4_1 (id INTEGER PRIMARY KEY)");
            }

            @Override
            public void down(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("DROP TABLE step_4_0");
                helper.execSQL("DROP TABLE step_4_1");
            }
        });
        migration.addStep(8, new ManualStepMigration.Step() {
            @Override
            public void up(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_8_0 (id INTEGER PRIMARY KEY)");
                helper.execSQL("CREATE TABLE step_8_1 (id INTEGER PRIMARY KEY)");
            }

            @Override
            public void down(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("DROP TABLE step_8_0");
                helper.execSQL("DROP TABLE step_8_1");
            }
        });

        migration.addStep(16, new ManualStepMigration.Step() {
            @Override
            public void up(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("CREATE TABLE step_16_0 (id INTEGER PRIMARY KEY)");
                helper.execSQL("CREATE TABLE step_16_1 (id INTEGER PRIMARY KEY)");
            }

            @Override
            public void down(@NonNull ManualStepMigration.Helper helper) {
                seq.add(new StepContext(helper.version, helper.upgrade));

                helper.execSQL("DROP TABLE step_16_0");
                helper.execSQL("DROP TABLE step_16_1");
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    @Test
    public void testIdempotenceWithNop() throws Exception {
        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());
        migration.start(db, new ArrayList<SchemaData>());
    }

    @Test
    public void upgradeFull() throws Exception {
        migration.upgrade(db, 1, 100);

        assertThat(migration.fetchDbVersion(db), is(16));

        assertThat(seq.size(), is(4));

        assertThat(seq.get(0).version, is(2));
        assertThat(seq.get(0).upgrade, is(true));

        assertThat(seq.get(1).version, is(4));
        assertThat(seq.get(1).upgrade, is(true));

        assertThat(seq.get(2).version, is(8));
        assertThat(seq.get(2).upgrade, is(true));

        assertThat(seq.get(3).version, is(16));
        assertThat(seq.get(3).upgrade, is(true));
    }

    @Test
    public void upgradeBoundary() throws Exception {
        migration.upgrade(db, 2, 8);

        assertThat(migration.fetchDbVersion(db), is(8));

        assertThat(seq.size(), is(2));

        assertThat(seq.get(0).version, is(4));
        assertThat(seq.get(0).upgrade, is(true));

        assertThat(seq.get(1).version, is(8));
        assertThat(seq.get(1).upgrade, is(true));
    }

    @Test
    public void downgradeFull() throws Exception {
        migration.upgrade(db, 1, 100);
        seq.clear();

        migration.downgrade(db, 100, 1);

        assertThat(migration.fetchDbVersion(db), lessThan(4));

        assertThat(seq.size(), is(4));

        assertThat(seq.get(0).version, is(16));
        assertThat(seq.get(0).upgrade, is(false));

        assertThat(seq.get(1).version, is(8));
        assertThat(seq.get(1).upgrade, is(false));

        assertThat(seq.get(2).version, is(4));
        assertThat(seq.get(2).upgrade, is(false));

        assertThat(seq.get(3).version, is(2));
        assertThat(seq.get(3).upgrade, is(false));
    }

    @Test
    public void downgradeBoundary() throws Exception {
        migration.upgrade(db, 1, 100);
        seq.clear();

        migration.downgrade(db, 8, 2);

        assertThat(migration.fetchDbVersion(db), lessThan(4));

        assertThat(seq.size(), is(2));

        assertThat(seq.get(0).version, is(8));
        assertThat(seq.get(0).upgrade, is(false));

        assertThat(seq.get(1).version, is(4));
        assertThat(seq.get(1).upgrade, is(false));
    }

    @Test
    public void upgradeAndDowngrade() throws Exception {
        {
            migration = new ManualStepMigration(getContext(), 100, true);
            setupSteps();
            migration.saveStep(db, 1, null);
            migration.start(db, new ArrayList<SchemaData>());

            assertThat(seq.size(), is(4));

            assertThat(seq.get(0).version, is(2));
            assertThat(seq.get(0).upgrade, is(true));

            assertThat(seq.get(1).version, is(4));
            assertThat(seq.get(1).upgrade, is(true));

            assertThat(seq.get(2).version, is(8));
            assertThat(seq.get(2).upgrade, is(true));

            assertThat(seq.get(3).version, is(16));
            assertThat(seq.get(3).upgrade, is(true));
        }

        seq.clear();

        {
            migration = new ManualStepMigration(getContext(), 1, true);
            setupSteps();
            migration.start(db, new ArrayList<SchemaData>());

            assertThat(seq.size(), is(4));

            assertThat(seq.get(0).version, is(16));
            assertThat(seq.get(0).upgrade, is(false));

            assertThat(seq.get(1).version, is(8));
            assertThat(seq.get(1).upgrade, is(false));

            assertThat(seq.get(2).version, is(4));
            assertThat(seq.get(2).upgrade, is(false));

            assertThat(seq.get(3).version, is(2));
            assertThat(seq.get(3).upgrade, is(false));
        }
    }

    static class StepContext {

        final int version;

        final boolean upgrade;

        public StepContext(int version, boolean upgrade) {
            this.version = version;
            this.upgrade = upgrade;
        }
    }
}
