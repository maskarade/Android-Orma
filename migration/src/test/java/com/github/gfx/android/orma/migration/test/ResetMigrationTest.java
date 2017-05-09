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

import com.github.gfx.android.orma.migration.ResetMigration;
import com.github.gfx.android.orma.migration.SQLiteMaster;
import com.github.gfx.android.orma.migration.TraceListener;
import com.github.gfx.android.orma.migration.test.util.OpenHelper;
import com.github.gfx.android.orma.migration.test.util.SchemaData;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ResetMigrationTest {

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testResetMigration() throws Exception {
        List<SchemaData> srcSchemas = Arrays.asList(
                new SchemaData("foo", "CREATE TABLE `foo` (`field01` TEXT, `field02` TEXT)",
                        "CREATE INDEX `index_field01_on_foo` ON `foo` (`field01`)",
                        "CREATE INDEX `index_field02_on_foo` ON `foo` (`field02`)"
                ),
                new SchemaData("bar", "CREATE TABLE `bar` (`field10` TEXT, `field20` TEXT)")
        );

        List<String> initialData = Arrays.asList(
                "INSERT INTO foo (field01, field02) VALUES ('value01', 'value02')",
                "INSERT INTO bar (field10, field10) VALUES ('value10', 'value10')"
        );

        List<SchemaData> dstSchemas = Collections.singletonList(new SchemaData("bar", "CREATE TABLE `bar` (`field10` TEXT)"));

        OpenHelper helper = new OpenHelper(getContext(), srcSchemas, initialData);

        ResetMigration migration = new ResetMigration(TraceListener.EMPTY);
        migration.start(helper.getWritableDatabase(), dstSchemas);

        Collection<SQLiteMaster> schemas = SQLiteMaster.loadTables(helper.getReadableDatabase()).values();
        assertThat(schemas, hasSize(1));
        for (SQLiteMaster schema : schemas) {
            assertThat(schema.getCreateTableStatement(), is(dstSchemas.get(0).getCreateTableStatement()));
        }
    }
}
