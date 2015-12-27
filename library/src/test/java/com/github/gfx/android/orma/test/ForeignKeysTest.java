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
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ForeignKeysTest {

    OrmaDatabase db;

    Publisher publisher;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaDatabase.builder(getContext()).name(null).build();

        publisher = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "foo bar";
                publisher.startedYear = 2015;
                publisher.startedMonth = 12;

                return publisher;
            }
        });

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "today";
                book.content = "milk, banana";
                book.inPrint = true;
                book.publisher = SingleAssociation.id(publisher.id);
                return book;
            }
        });

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "friday";
                book.content = "apple";
                book.inPrint = false;
                book.publisher = SingleAssociation.id(publisher.id);
                return book;
            }
        });
    }

    @Test
    public void testHasOne() throws Exception {
        Publisher publisher = db.selectFromBook().value().publisher.single().toBlocking().value();
        assertThat(publisher.name, is("foo bar"));
        assertThat(publisher.startedYear, is(2015));
        assertThat(publisher.startedMonth, is(12));
    }

    @Test
    public void testHasManyRelation() throws Exception {
        final Publisher a = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "A";
                return publisher;
            }
        });

        final Publisher b = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "B";
                return publisher;
            }
        });

        for (int i = 0; i < 2; i++) {
            final int x = i;
            db.createBook(new ModelFactory<Book>() {
                @NonNull
                @Override
                public Book call() {
                    Book book = new Book();
                    book.publisher = SingleAssociation.id(a.id);
                    book.title = "a " + x;
                    return book;
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            final int x = i;

            db.createBook(new ModelFactory<Book>() {
                @NonNull
                @Override
                public Book call() {
                    Book book = new Book();
                    book.publisher = SingleAssociation.id(b.id);
                    book.title = "b " + x;
                    return book;
                }
            });
        }

        assertThat(a.books(db).count(), is(2));
        assertThat(b.books(db).count(), is(3));
    }

    @Test
    public void updateSingleAssociation() throws Exception {
        Publisher publisher = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "The Nova";
                publisher.startedYear = 2015;
                publisher.startedMonth = 12;
                return publisher;
            }
        });

        int count = db.updateBook()
                .where("title = ?", "today")
                .publisher(publisher)
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().where("title = ?", "today").value();
        assertThat(book.publisher.single().toBlocking().value().name, is("The Nova"));
    }

    @Test
    public void updateSingleAssociationByReference() throws Exception {
        Publisher publisher = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "The Nova";
                publisher.startedYear = 2015;
                publisher.startedMonth = 12;
                return publisher;
            }
        });

        int count = db.updateBook()
                .where("title = ?", "today")
                .publisher(SingleAssociation.<Publisher>id(publisher.id))
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().where("title = ?", "today").value();
        assertThat(book.publisher.single().toBlocking().value().name, is("The Nova"));
    }

    @Test(expected = SQLiteException.class)
    public void testUpdateOnCascade() throws Exception {
        db.updateBook().publisher(SingleAssociation.<Publisher>id(0L)).execute();
    }

    @Test
    public void testDeleteOnCascade() throws Exception {
        db.deleteFromPublisher().execute();
        assertThat(db.selectFromBook().count(), is(0));
    }
}
