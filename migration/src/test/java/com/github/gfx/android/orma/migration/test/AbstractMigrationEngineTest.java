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

import com.github.gfx.android.orma.core.Database;
import com.github.gfx.android.orma.core.DefaultDatabase;
import com.github.gfx.android.orma.migration.AbstractMigrationEngine;
import com.github.gfx.android.orma.migration.MigrationSchema;
import com.github.gfx.android.orma.migration.TraceListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class AbstractMigrationEngineTest {

    static class MyEngine extends AbstractMigrationEngine {

        protected MyEngine() {
            super(TraceListener.EMPTY);
        }

        @NonNull
        @Override
        public String getTag() {
            return getClass().getSimpleName();
        }

        @Override
        public void start(@NonNull Database db, @NonNull List<? extends MigrationSchema> schemas) {
            throw new UnsupportedOperationException();
        }
    }

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    public void testTransaction() throws Exception {
        MyEngine myEngine = new MyEngine();

        final Database db = new DefaultDatabase.Provider().provideOnMemoryDatabase(getContext());
        db.setForeignKeyConstraintsEnabled(true);

        assertThat(db.longForQuery("PRAGMA foreign_keys", null), is(1L));

        myEngine.transaction(db, new Runnable() {
            @Override
            public void run() {
                assertThat(db.longForQuery("PRAGMA foreign_keys", null), is(0L));
            }
        });

        assertThat(db.longForQuery("PRAGMA foreign_keys", null), is(1L));
    }
}
