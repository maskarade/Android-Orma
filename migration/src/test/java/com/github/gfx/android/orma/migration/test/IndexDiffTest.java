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

import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class IndexDiffTest {

    static final String SCHEMA_HASH = "abc";

    SchemaDiffMigration migration;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        migration = new SchemaDiffMigration(getContext(), SCHEMA_HASH);
    }


    @Test
    public void buildDropIndexStatement() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void buildDropIndexStatement_caseInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create index if not exists index_foo on foo (bar)"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void buildDropIndexStatement_spaceInsensitive() throws Exception {
        assertThat(migration.buildDropIndexStatement("create \n"
                        + "index \n"
                        + "if \n"
                        + "not \n"
                        + "exists \n"
                        + "index_foo \n"
                        + "on \n"
                        + "foo \n"
                        + "(bar)\n"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void buildDropIndexStatement_omitIfNotExists() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void buildDropIndexStatement_doubleQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS `index_foo` ON `foo` (`bar`)"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void buildDropIndexStatement_backQuotedNames() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX IF NOT EXISTS `index_foo` ON `foo` (`bar`)"),
                is("DROP INDEX IF EXISTS `index_foo`"));
    }

    @Test
    public void testIndexDiff() throws Exception {
        assertThat(migration.indexDiff(Collections.<String>emptyList(), Arrays.asList(
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`)",
                "CREATE INDEX IF NOT EXISTS `index_bar` ON `t` (`bar`)"
        )), contains(
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`)",
                "CREATE INDEX IF NOT EXISTS `index_bar` ON `t` (`bar`)"
        ));
    }

    @Test
    public void indexDiff_removeUnique() throws Exception {
        assertThat(migration.indexDiff(Collections.singletonList(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`)"
        ), Collections.singletonList(
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`)"
        )), contains(
                "DROP INDEX IF EXISTS `index_foo`",
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`)"
        ));
    }


    @Test
    public void indexDiff_multiColumnIndex() throws Exception {
        assertThat(migration.indexDiff(Collections.<String>emptyList(), Collections.singletonList(
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`, `bar`)"
        )), contains(
                "CREATE INDEX IF NOT EXISTS `index_foo` ON `t` (`foo`, `bar`)"
        ));
    }
}



