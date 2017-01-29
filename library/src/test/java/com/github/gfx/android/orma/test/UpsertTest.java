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
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class UpsertTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
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
    public void upsertAsUpdateForModelsWitAutoId() throws Exception {
        Publisher publisher = Publisher.create("foo", 2000, 12);
        Publisher newPublisher = db.relationOfPublisher().upsert(publisher);
        newPublisher.startedYear = 1999;
        assertThat(db.relationOfPublisher().upsert(newPublisher).startedYear, is(1999));
    }

}
