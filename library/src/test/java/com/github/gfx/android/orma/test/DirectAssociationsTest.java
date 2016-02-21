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

import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Author_Selector;
import com.github.gfx.android.orma.test.model.ModelWithDirectAssociation;
import com.github.gfx.android.orma.test.model.ModelWithDirectAssociation_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class DirectAssociationsTest {

    OrmaDatabase orma;

    Author author1;

    Author author2;

    @Before
    public void setUp() throws Exception {
        orma = OrmaFactory.create();

        author1 = orma.createAuthor(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "A";
                author.note = "A's note";
                return author;
            }
        });

        author2 = orma.createAuthor(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "B";
                author.note = "B's note";
                return author;
            }
        });
    }

    @Test
    public void testCreate() throws Exception {
        ModelWithDirectAssociation model = orma.createModelWithDirectAssociation(
                new ModelFactory<ModelWithDirectAssociation>() {
                    @NonNull
                    @Override
                    public ModelWithDirectAssociation call() {
                        ModelWithDirectAssociation model = new ModelWithDirectAssociation();
                        model.title = "foo";
                        model.author = author1;
                        model.note = "SQLite rocks";
                        return model;
                    }
                });

        assertThat(model.title, is("foo"));
        assertThat(model.note, is("SQLite rocks"));
        assertThat(model.author, is(notNullValue()));
        assertThat(model.author.name, is(author1.name));
        assertThat(model.author.note, is(author1.note));
    }

    @Test
    public void testUpdate() throws Exception {
        orma.createModelWithDirectAssociation(
                new ModelFactory<ModelWithDirectAssociation>() {
                    @NonNull
                    @Override
                    public ModelWithDirectAssociation call() {
                        ModelWithDirectAssociation model = new ModelWithDirectAssociation();
                        model.title = "foo";
                        model.author = author1;
                        model.note = "SQLite rocks";
                        return model;
                    }
                });

        orma.updateModelWithDirectAssociation()
                .authorEq(author1)
                .author(author2)
                .execute();

        ModelWithDirectAssociation model = orma.selectFromModelWithDirectAssociation().value();

        assertThat(model.title, is("foo"));
        assertThat(model.note, is("SQLite rocks"));
        assertThat(model.author, is(notNullValue()));
        assertThat(model.author.name, is(author2.name));
        assertThat(model.author.note, is(author2.note));
    }

    @Test
    public void testDelete() throws Exception {
        orma.createModelWithDirectAssociation(
                new ModelFactory<ModelWithDirectAssociation>() {
                    @NonNull
                    @Override
                    public ModelWithDirectAssociation call() {
                        ModelWithDirectAssociation model = new ModelWithDirectAssociation();
                        model.title = "foo";
                        model.author = author1;
                        model.note = "SQLite rocks";
                        return model;
                    }
                });

        orma.deleteFromModelWithDirectAssociation()
                .authorEq(author1)
                .execute();

        assertThat(orma.selectFromModelWithDirectAssociation().isEmpty(), is(true));
    }

    @Test
    public void testFindByPrimaryKey() throws Exception {
        Inserter<ModelWithDirectAssociation> inserter = orma.prepareInsertIntoModelWithDirectAssociation();
        inserter.execute(new ModelFactory<ModelWithDirectAssociation>() {
            @NonNull
            @Override
            public ModelWithDirectAssociation call() {
                ModelWithDirectAssociation model = new ModelWithDirectAssociation();
                model.title = "foo";
                model.author = author1;
                model.note = "SQLite rocks";
                return model;
            }
        });
        inserter.execute(new ModelFactory<ModelWithDirectAssociation>() {
            @NonNull
            @Override
            public ModelWithDirectAssociation call() {
                ModelWithDirectAssociation model = new ModelWithDirectAssociation();
                model.title = "bar";
                model.author = author2;
                model.note = "SQLite supports most of SQL92";
                return model;
            }
        });

        ModelWithDirectAssociation_Selector selector = orma.selectFromModelWithDirectAssociation().authorEq(author1);

        assertThat(selector.count(), is(1));

        ModelWithDirectAssociation model = selector.value();
        assertThat(model.title, is("foo"));
        assertThat(model.note, is("SQLite rocks"));
        assertThat(model.author, is(notNullValue()));
        assertThat(model.author.name, is(author1.name));
        assertThat(model.author.note, is(author1.note));
    }
}
