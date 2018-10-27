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

import com.github.gfx.android.orma.migration.SqliteDdlBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class UtilityTest {

    SqliteDdlBuilder util;

    @Before
    public void setUp() throws Exception {
        util = new SqliteDdlBuilder();
    }

    @Test
    public void renameTable() throws Exception {
        assertThat(util.buildRenameTable("foo", "bar"), is("ALTER TABLE `foo` RENAME TO `bar`"));
    }

    @Test
    public void renameColumn() throws Exception {
        assertThat(util.buildRenameColumn("CREATE TABLE foo (id INTEGER PRIMARY KEY, bar TEXT)", "bar", "baz"),
                contains(
                        "CREATE TABLE `__temp_foo` (`id` INTEGER PRIMARY KEY, `baz` TEXT)",
                        "INSERT INTO `__temp_foo` (`id`, `baz`) SELECT `id`, `bar` FROM `foo`",
                        "DROP TABLE `foo`",
                        "ALTER TABLE `__temp_foo` RENAME TO `foo`"
                ));
    }
}
