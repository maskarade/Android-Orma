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
package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.OrmaMigration;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class MigrationEngineTest {

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void startEmpty() throws Exception {
        MigrationEngine migration = OrmaMigration.builder(getContext())
                .versionForManualStepMigration(1)
                .schemaHashForSchemaDiffMigration(OrmaDatabase.SCHEMA_HASH)
                .build();

        OrmaConnection conn = OrmaFactory.builder()
                .migrationEngine(migration)
                .build()
                .getConnection();

        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
    }

    @Test
    public void justRun() throws Exception {
        final AtomicInteger value = new AtomicInteger();

        OrmaDatabase orma = OrmaFactory.builder()
                .versionForManualStepMigration(100)
                .migrationStep(10, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        value.addAndGet(10);
                    }
                })
                .migrationStep(11, new ManualStepMigration.ChangeStep() {
                    @Override
                    public void change(@NonNull ManualStepMigration.Helper helper) {
                        value.addAndGet(11);
                    }
                })
                .build();

        orma.migrate();

        assertThat("No migration on initialization", value.get(), is(0));
    }
}
