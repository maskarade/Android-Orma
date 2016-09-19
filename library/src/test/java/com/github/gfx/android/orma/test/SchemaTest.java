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
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.test.model.Author_Schema;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.ModelWithDirectAssociation_Schema;
import com.github.gfx.android.orma.test.model.ModelWithInheritance;
import com.github.gfx.android.orma.test.model.ModelWithInheritance_Schema;
import com.github.gfx.android.orma.test.model.ModelWithPrimaryKeyIsNotFirst;
import com.github.gfx.android.orma.test.model.ModelWithPrimaryKeyIsNotFirst_Schema;
import com.github.gfx.android.orma.test.model.ModelWithStorageTypes_Schema;
import com.github.gfx.android.orma.test.model.ModelWithTypeAdapters_Schema;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.model.PublisherSchema;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class SchemaTest {

    @Test
    public void testPublisherSchema() throws Exception {
        PublisherSchema schema = PublisherSchema.INSTANCE;

        assertThat(schema.getTableName(), is("publishers"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) schema.id));

        assertThat(schema.name.name, is("name"));
        assertThat(schema.name.schema, is((Schema<Publisher>) schema));
        assertThat(schema.name.getEscapedName(), is(notNullValue()));
        assertThat(schema.name.getFullyQualifiedName(), is(notNullValue()));

        assertThat(schema.startedYear.name, is("started_year"));
        assertThat(schema.startedMonth.name, is("started_month"));

        assertThat("PRIMARY KEY is placed in the last",
                schema.getCreateTableStatement(), is(
                        "CREATE TABLE `publishers` (`name` TEXT UNIQUE NOT NULL, `started_year` INTEGER NOT NULL, `started_month` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)"
                ));
    }

    @Test
    public void testBookSchema() throws Exception {
        Book_Schema schema = Book_Schema.INSTANCE;

        assertThat(schema.getTableName(), is("Book"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) schema.bookId));

        assertThat(schema.bookId.name, is("bookId"));
        assertThat(schema.bookId.storageType, is("INTEGER"));
        assertThat(schema.title.name, is("title"));
        assertThat(schema.title.storageType, is("TEXT"));
        assertThat(schema.content.name, is("content"));
        assertThat(schema.content.storageType, is("TEXT"));
        assertThat(schema.publisher.name, is("publisher"));
        assertThat(schema.publisher.storageType, is("INTEGER"));
    }

    @Test
    public void testAuthorSchema() throws Exception {
        Author_Schema schema = Author_Schema.INSTANCE;

        assertThat(schema.getTableName(), is("Author"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef) schema.name));
    }

    @Test
    public void testPrimaryKeyAttributes() throws Exception {
        assertThat(PublisherSchema.INSTANCE.id.isPrimaryKey(), is(true));
        assertThat(PublisherSchema.INSTANCE.id.isAutoincremnt(), is(true));
        assertThat(PublisherSchema.INSTANCE.id.isAutoValue(), is(true));

        assertThat(Book_Schema.INSTANCE.bookId.isPrimaryKey(), is(true));
        assertThat(Book_Schema.INSTANCE.bookId.isAutoincremnt(), is(false));
        assertThat(Book_Schema.INSTANCE.bookId.isAutoValue(), is(true));

        assertThat(Author_Schema.INSTANCE.name.isPrimaryKey(), is(true));
        assertThat(Author_Schema.INSTANCE.name.isAutoincremnt(), is(false));
        assertThat(Author_Schema.INSTANCE.name.isAutoValue(), is(false));
    }

    @Test
    public void testColumnStorageTypes() throws Exception {
        ModelWithStorageTypes_Schema schema = ModelWithStorageTypes_Schema.INSTANCE;
        assertThat(schema.date.storageType, is("INTEGER"));
        assertThat(schema.timestamp.storageType, is("DATETIME"));
        assertThat(schema.INSTANCE.getCreateTableStatement(), is(
                "CREATE TABLE `ModelWithStorageTypes` (`date` INTEGER NOT NULL, `timestamp` DATETIME NOT NULL)"
        ));
    }

    @Test
    public void testStaticTypeAdapterStorageTypes() throws Exception {
        ModelWithTypeAdapters_Schema schema = ModelWithTypeAdapters_Schema.INSTANCE;
        assertThat(schema.date.storageType, is("INTEGER"));
        assertThat(schema.sqlDate.storageType, is("TEXT"));
        assertThat(schema.sqlTime.storageType, is("TEXT"));
        assertThat(schema.sqlTimestamp.storageType, is("TEXT"));
        assertThat(schema.intTuple2.storageType, is("INTEGER"));
    }

    @Test
    public void testStorageTypeForDirectAssociation() throws Exception {
        assertThat(ModelWithDirectAssociation_Schema.INSTANCE.author.storageType,
                is(Author_Schema.INSTANCE.getPrimaryKey().storageType));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInheritance() throws Exception {
        List<ColumnDef<ModelWithInheritance, ?>> columns = ModelWithInheritance_Schema.INSTANCE.getColumns();

        assertThat("Base columns first, PrimaryKey las...t",
                columns,
                Matchers.<ColumnDef<ModelWithInheritance, ?>>contains(
                        ModelWithInheritance_Schema.INSTANCE.baseColumn,
                        ModelWithInheritance_Schema.INSTANCE.value,
                        ModelWithInheritance_Schema.INSTANCE.id
                ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPrimaryKeyPosition() throws Exception {
        List<ColumnDef<ModelWithPrimaryKeyIsNotFirst, ?>> columns = ModelWithPrimaryKeyIsNotFirst_Schema.INSTANCE.getColumns();

        assertThat("Base columns first, PrimaryKey las...t",
                columns,
                Matchers.<ColumnDef<ModelWithPrimaryKeyIsNotFirst, ?>>contains(
                        ModelWithPrimaryKeyIsNotFirst_Schema.INSTANCE.foo,
                        ModelWithPrimaryKeyIsNotFirst_Schema.INSTANCE.bar,
                        ModelWithPrimaryKeyIsNotFirst_Schema.INSTANCE.id
                ));
    }
}
