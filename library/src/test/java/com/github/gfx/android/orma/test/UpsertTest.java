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

import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.ModelWithDirectAssociation;
import com.github.gfx.android.orma.test.model.ModelWithNullableDirectAssociations;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * {@link com.github.gfx.android.orma.test.model.ModelWithDirectAssociation_Relation#upsertWithoutTransaction(ModelWithDirectAssociation)}
 */
@RunWith(AndroidJUnit4.class)
public class UpsertTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
    }

    @After
    public void tearDown() throws Exception {
        db.getConnection().getWritableDatabase().close();
    }

    @Test
    public void insertOrReplaceAsInsert() throws Exception {
        Author author = Author.create("foo");
        db.prepareInsertIntoAuthor(OnConflict.REPLACE).execute(author);
        assertThat(db.selectFromAuthor().value().name, is("foo"));
    }

    @Test
    public void insertOrReplaceAsReplace() throws Exception {
        Author author = Author.create("foo");
        db.prepareInsertIntoAuthor(OnConflict.REPLACE).execute(author);
        author.note = "note";
        db.prepareInsertIntoAuthor(OnConflict.REPLACE).execute(author);

        assertThat(db.selectFromAuthor().value().note, is("note"));
    }

    @Test
    public void upsertAsInsertForModelsWithoutAutoId() throws Exception {
        Author author = Author.create("foo");
        db.relationOfAuthor().upsert(author);
        assertThat(db.selectFromAuthor().value().name, is("foo"));
    }

    @Test
    public void upsertAsUpdateForModelsWithoutAutoId() throws Exception {
        Author author = Author.create("foo");
        Author newAuthor = db.relationOfAuthor().upsert(author);
        newAuthor.note = "note";
        assertThat(db.relationOfAuthor().upsert(newAuthor).note, is("note"));
    }

    @Test
    public void upsertAsInsertForModelsWithAutoId() throws Exception {
        Publisher publisher = Publisher.create("foo", 2000, 12);
        db.relationOfPublisher().upsert(publisher);
        assertThat(db.selectFromPublisher().value().name, is("foo"));
    }

    @Test
    public void upsertAsUpdateForModelsWithAutoId() throws Exception {
        Publisher publisher = Publisher.create("foo", 2000, 12);
        Publisher newPublisher = db.relationOfPublisher().upsert(publisher);
        newPublisher.startedYear = 1999;
        assertThat(db.relationOfPublisher().upsert(newPublisher).startedYear, is(1999));
    }

    @Test
    public void upsertAsInsertForModelsWithSingleAssociation() throws Exception {
        Book book = Book.create("foo", Publisher.create("bar", 2017, 1));
        Book newBook = db.relationOfBook().upsert(book);
        assertThat(newBook.bookId, is(not(0L)));
        assertThat(newBook.publisher.get().name, is("bar"));
    }

    @Test
    public void upsertAsUpdateForModelsWithSingleAssociation() throws Exception {
        Book book = Book.create("foo", Publisher.create("bar", 2017, 1));
        Book newBook = db.relationOfBook().upsert(book);
        newBook.content = "Hello, world!";
        assertThat(db.relationOfBook().upsert(newBook).content, is("Hello, world!"));
    }

    @Test
    public void upsertAsInsertForModelsWithDirectAssociation() throws Exception {
        ModelWithDirectAssociation model = ModelWithDirectAssociation
                .create("foo", Author.create("author"), Publisher.create("publisher", 2017, 1), "note");
        ModelWithDirectAssociation newModel = db.relationOfModelWithDirectAssociation().upsert(model);
        assertThat(newModel.name, is("foo"));
        assertThat(newModel.author.name, is("author"));
        assertThat(newModel.publisher.name, is("publisher"));
    }

    @Test
    public void upsertAsUpdateForModelsWithDirectAssociation() throws Exception {
        ModelWithDirectAssociation model = ModelWithDirectAssociation
                .create("foo", Author.create("author"), Publisher.create("publisher", 2017, 1), "note");
        ModelWithDirectAssociation newModel = db.relationOfModelWithDirectAssociation().upsert(model);
        newModel.note = "model's note";
        newModel.author.note = "author's note";

        assertThat(db.relationOfModelWithDirectAssociation().upsert(newModel).note, is("model's note"));
        assertThat(db.relationOfModelWithDirectAssociation().upsert(newModel).author.note, is("author's note"));
    }

    @Test
    public void upsertAsInsertForModelsWithNullableDirectAssociation() throws Exception {
        ModelWithNullableDirectAssociations model = new ModelWithNullableDirectAssociations();
        ModelWithNullableDirectAssociations newModel = db.relationOfModelWithNullableDirectAssociations().upsert(model);
        assertThat(newModel.id, is(not(0L)));
        assertThat(newModel.author, is(nullValue()));
    }

    @Test
    public void upsertAsUpdateForModelsWithNullableDirectAssociation() throws Exception {
        ModelWithNullableDirectAssociations model = new ModelWithNullableDirectAssociations();
        ModelWithNullableDirectAssociations newModel = db.relationOfModelWithNullableDirectAssociations().upsert(model);
        newModel.author = Author.create("foo");
        assertThat(db.relationOfModelWithNullableDirectAssociations().upsert(newModel).author, is(notNullValue()));
    }

    // -------------------------

    @Test
    public void others() throws Exception {
        Author author = Author.create("foo");

        // for Iterable<T>
        assertThat(db.relationOfAuthor().upsert(Collections.singleton(author)), contains(author));

        // as single
        db.relationOfAuthor().upsertAsSingle(author)
                .test()
                .assertResult(author);


        // as observable
        db.relationOfAuthor().upsertAsObservable(Collections.singleton(author))
                .test()
                .assertResult(author);

    }

}
