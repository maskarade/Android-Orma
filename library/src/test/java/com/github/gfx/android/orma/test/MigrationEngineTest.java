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
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.OrmaMigration;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class MigrationEngineTest {

    MigrationEngine migration;

    OrmaConnection conn;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        migration = new OrmaMigration(getContext(), 1, false);
        conn = OrmaDatabase.builder(getContext())
                .name(null)
                .migrationEngine(migration)
                .tryParsingSql(false)
                .build()
                .getConnection();
    }

    @Test
    public void startEmpty() throws Exception {
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
        migration.start(conn.getWritableDatabase(), conn.getSchemas());
    }
}
