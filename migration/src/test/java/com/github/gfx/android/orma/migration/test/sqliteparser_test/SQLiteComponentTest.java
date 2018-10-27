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

package com.github.gfx.android.orma.migration.test.sqliteparser_test;

import com.github.gfx.android.orma.migration.sqliteparser.SQLiteComponent;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @see SQLiteComponent
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteComponentTest {

    @Test
    public void testKeywordEquals() throws Exception {
        SQLiteComponent.Keyword a = new SQLiteComponent.Keyword("foo");
        SQLiteComponent.Keyword b = new SQLiteComponent.Keyword("foo");

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void testKeywordCaseInsensitiveEquals() throws Exception {
        SQLiteComponent.Keyword a = new SQLiteComponent.Keyword("foo");
        SQLiteComponent.Keyword b = new SQLiteComponent.Keyword("FOO");

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void testNameEquals() throws Exception {
        SQLiteComponent.Name a = new SQLiteComponent.Name("foo");
        SQLiteComponent.Name b = new SQLiteComponent.Name("foo");

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void testNameCaseInsensitiveEquals() throws Exception {
        SQLiteComponent.Name a = new SQLiteComponent.Name("foo");
        SQLiteComponent.Name b = new SQLiteComponent.Name("FOO");

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void testNameQuoting() throws Exception {
        SQLiteComponent.Name a = new SQLiteComponent.Name("`foo`");
        SQLiteComponent.Name b = new SQLiteComponent.Name("\"foo\"");

        assertThat(a, is(b));
        assertThat(a.hashCode(), is(b.hashCode()));

        assertThat(a.toString(), is("`foo`"));
    }
}
