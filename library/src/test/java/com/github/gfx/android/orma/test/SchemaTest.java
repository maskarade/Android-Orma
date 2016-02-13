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

import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.test.model.Author_Schema;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.ModelWithStorageTypes_Schema;
import com.github.gfx.android.orma.test.model.ModelWithTypeAdapters_Schema;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.PublisherSchema;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class SchemaTest {

    @Test
    public void testPublisherSchema() throws Exception {
        PublisherSchema schema = OrmaDatabase.schemaPublisher;

        assertThat(schema.getTableName(), is("publishers"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) PublisherSchema.id));

        assertThat(PublisherSchema.name.name, is("name"));
        assertThat(PublisherSchema.startedYear.name, is("started_year"));
        assertThat(PublisherSchema.startedMonth.name, is("started_month"));

        assertThat("PRIMARY KEY is placed in the last",
                schema.getCreateTableStatement(), is(
                        "CREATE TABLE \"publishers\" (\"name\" TEXT UNIQUE NOT NULL, \"started_year\" INTEGER NOT NULL, \"started_month\" INTEGER NOT NULL, \"id\" INTEGER PRIMARY KEY AUTOINCREMENT)"
                ));
    }

    @Test
    public void testBookSchema() throws Exception {
        Book_Schema schema = OrmaDatabase.schemaBook;

        assertThat(schema.getTableName(), is("Book"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) Book_Schema.bookId));

        assertThat(Book_Schema.bookId.name, is("bookId"));
        assertThat(Book_Schema.bookId.storageType, is("INTEGER"));
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
        assertThat(PublisherSchema.id.isPrimaryKey(), is(true));
        assertThat(PublisherSchema.id.isAutoincremnt(), is(true));
        assertThat(PublisherSchema.id.isAutoValue(), is(true));

        assertThat(Book_Schema.bookId.isPrimaryKey(), is(true));
        assertThat(Book_Schema.bookId.isAutoincremnt(), is(false));
        assertThat(Book_Schema.bookId.isAutoValue(), is(true));

        assertThat(Author_Schema.name.isPrimaryKey(), is(true));
        assertThat(Author_Schema.name.isAutoincremnt(), is(false));
        assertThat(Author_Schema.name.isAutoValue(), is(false));
    }

    @Test
    public void testColumnStorageTypes() throws Exception {
        assertThat(ModelWithStorageTypes_Schema.date.storageType, is("INTEGER"));
        assertThat(ModelWithStorageTypes_Schema.timestamp.storageType, is("DATETIME"));
        assertThat(ModelWithStorageTypes_Schema.INSTANCE.getCreateTableStatement(), is(
                "CREATE TABLE \"ModelWithStorageTypes\" (\"date\" INTEGER NOT NULL, \"timestamp\" DATETIME NOT NULL)"
        ));
    }

    @Test
    public void testStaticTypeAdapterStorageTypes() throws Exception {
        assertThat(ModelWithTypeAdapters_Schema.date.storageType, is("INTEGER"));
        assertThat(ModelWithTypeAdapters_Schema.sqlDate.storageType, is("TEXT"));
        assertThat(ModelWithTypeAdapters_Schema.sqlTime.storageType, is("TEXT"));
        assertThat(ModelWithTypeAdapters_Schema.sqlTimestamp.storageType, is("TEXT"));
        assertThat(ModelWithTypeAdapters_Schema.intTuple2.storageType, is("INTEGER"));
    }

}
