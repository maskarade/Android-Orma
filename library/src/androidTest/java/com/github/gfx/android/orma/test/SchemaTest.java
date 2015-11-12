package com.github.gfx.android.orma.test;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SchemaTest {

    @Test
    public void testPublisherSchema() throws Exception {
        PublisherSchema schema = OrmaDatabase.schemaPublisher;

        assertThat(schema.getTableName(), is("publishers"));

        assertThat(PublisherSchema.name.name, is("name"));
        assertThat(PublisherSchema.startedYear.name, is("started_year"));
        assertThat(PublisherSchema.startedMonth.name, is("started_month"));
    }

    @Test
    public void testBookSchema() throws Exception {
        Book_Schema schema = OrmaDatabase.schemaBook;

        assertThat(schema.getTableName(), is("Book"));

        assertThat(Book_Schema.id.name, is("id"));
        assertThat(Book_Schema.title.name, is("title"));
        assertThat(Book_Schema.content.name, is("content"));
    }

}
