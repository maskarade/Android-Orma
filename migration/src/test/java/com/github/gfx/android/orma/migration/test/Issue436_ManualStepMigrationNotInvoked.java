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
import com.github.gfx.android.orma.migration.ManualStepMigration;
import com.github.gfx.android.orma.migration.test.util.SchemaData;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class Issue436_ManualStepMigrationNotInvoked {
    static {
        System.setProperty("robolectric.logging", "stderr");
    }

    Context getContext() {
        return ApplicationProvider.getApplicationContext();
    }

    Database createDatabase() {
        return new DefaultDatabase.Provider().provideOnMemoryDatabase(getContext());
    }

    @Test
    public void invokedWithoutSteps() {
        int initialVersion = 5;
        int secondVersion = 7;

        Database db = createDatabase();
        new ManualStepMigration(getContext(), initialVersion, true)
                .start(db, new ArrayList<SchemaData>());
        assertThat(db.getVersion(), is(initialVersion));


        ManualStepMigration m = new ManualStepMigration(getContext(), secondVersion, true);
        m.start(db, new ArrayList<SchemaData>());
        assertThat(db.getVersion(), is(secondVersion));

        db.close();
    }

    @Test
    public void invokedWithSteps() {
        int initialVersion = 1;
        int secondVersion = 3;

        Database db = createDatabase();
        new ManualStepMigration(getContext(), initialVersion, true)
                .start(db, new ArrayList<SchemaData>());
        assertThat(db.getVersion(), is(initialVersion));

        ManualStepMigration m = new ManualStepMigration(getContext(), secondVersion, true);
        m.addStep(secondVersion, new ManualStepMigration.ChangeStep() {
            @Override
            public void change(@NonNull ManualStepMigration.Helper helper) {
                // nop
            }
        });
        m.start(db, new ArrayList<SchemaData>());
        assertThat(db.getVersion(), is(secondVersion));

        db.close();
    }
}
