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

import com.github.gfx.android.orma.migration.BuildConfig;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.OrmaMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class OrmaMigrationTest {

    static int VERSION = 100;

    SQLiteDatabase db;

    OrmaMigration engine;

    static Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = SQLiteDatabase.create(null);

        engine = new OrmaMigration(getContext(), VERSION, BuildConfig.DEBUG);
        engine.addStep(2, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_2 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(4, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_4 (id INTEGER PRIMARY KEY)");
            }
        });
        engine.addStep(8, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_8 (id INTEGER PRIMARY KEY)");
            }
        });

        engine.addStep(16, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                helper.execSQL("CREATE TABLE step_16 (id INTEGER PRIMARY KEY)");
            }
        });
    }

    @Test
    public void testIdempotenceWithNop() throws Exception {
        engine.start(db, new ArrayList<SchemaData>());
        engine.start(db, new ArrayList<SchemaData>());
        engine.start(db, new ArrayList<SchemaData>());
    }
}
