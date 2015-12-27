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

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.test.model.Author_Schema;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.PublisherSchema;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class SchemaTest {

    @Test
    public void testPublisherSchema() throws Exception {
        PublisherSchema schema = OrmaDatabase.schemaPublisher;

        assertThat(schema.getTableName(), is("publishers"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) PublisherSchema.id));

        assertThat(PublisherSchema.name.name, is("name"));
        assertThat(PublisherSchema.startedYear.name, is("started_year"));
        assertThat(PublisherSchema.startedMonth.name, is("started_month"));
    }

    @Test
    public void testBookSchema() throws Exception {
        Book_Schema schema = OrmaDatabase.schemaBook;

        assertThat(schema.getTableName(), is("Book"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) Book_Schema.id));

        assertThat(Book_Schema.id.name, is("id"));
        assertThat(Book_Schema.id.storageType, is("INTEGER"));
        assertThat(Book_Schema.title.name, is("title"));
        assertThat(Book_Schema.title.storageType, is("TEXT"));
        assertThat(Book_Schema.content.name, is("content"));
        assertThat(Book_Schema.content.storageType, is("TEXT"));
        assertThat(Book_Schema.publisher.name, is("publisher"));
        assertThat(Book_Schema.publisher.storageType, is("INTEGER"));
    }

    @Test
    public void testAuthorSchema() throws Exception {
        Author_Schema schema = OrmaDatabase.schemaAuthor;

        assertThat(schema.getTableName(), is("Author"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) Author_Schema.name));
    }

    @Test
    public void testPrimaryKeyAttributes() throws Exception {
        assertThat(PublisherSchema.id.primaryKey, is(true));
        assertThat(PublisherSchema.id.autoincrement, is(true));
        assertThat(PublisherSchema.id.autoId, is(true));

        assertThat(Book_Schema.id.primaryKey, is(true));
        assertThat(Book_Schema.id.autoincrement, is(false));
        assertThat(Book_Schema.id.autoId, is(true));

        assertThat(Author_Schema.name.primaryKey, is(true));
        assertThat(Author_Schema.name.autoincrement, is(false));
        assertThat(Author_Schema.name.autoId, is(false));
    }
}
