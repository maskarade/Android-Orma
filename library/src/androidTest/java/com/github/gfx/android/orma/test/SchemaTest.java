package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.test.model.Author_Schema;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.PublisherSchema;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SchemaTest {

    @Test
    public void testPublisherSchema() throws Exception {
        PublisherSchema schema = OrmaDatabase.schemaPublisher;

        assertThat(schema.getTableName(), is("publishers"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef)PublisherSchema.id));

        assertThat(PublisherSchema.name.name, is("name"));
        assertThat(PublisherSchema.startedYear.name, is("started_year"));
        assertThat(PublisherSchema.startedMonth.name, is("started_month"));
    }

    @Test
    public void testBookSchema() throws Exception {
        Book_Schema schema = OrmaDatabase.schemaBook;

        assertThat(schema.getTableName(), is("Book"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef)Book_Schema.id));

        assertThat(Book_Schema.id.name, is("id"));
        assertThat(Book_Schema.title.name, is("title"));
        assertThat(Book_Schema.content.name, is("content"));
    }

    @Test
    public void testAuthorSchema() throws Exception {
        Author_Schema schema = OrmaDatabase.schemaAuthor;

        assertThat(schema.getTableName(), is("Author"));
        assertThat(schema.getPrimaryKey(), is((ColumnDef)Author_Schema.name));
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
