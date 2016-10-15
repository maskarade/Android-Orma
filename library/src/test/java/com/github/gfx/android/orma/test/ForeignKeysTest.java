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

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.ModelWithForeignKeyAction;
import com.github.gfx.android.orma.test.model.ModelWithForeignKeyAction_Schema;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * @see DirectAssociationsTest
 */
@RunWith(AndroidJUnit4.class)
public class ForeignKeysTest {

    OrmaDatabase db;

    Publisher publisher;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

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
        Publisher publisher = db.selectFromBook().value().publisher.observable().toBlocking().value();
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
                .titleEq("today")
                .publisher(publisher)
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().titleEq("today").value();
        assertThat(book.publisher.observable().toBlocking().value().name, is("The Nova"));
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
                .titleEq("today")
                .publisher(SingleAssociation.<Publisher>id(publisher.id))
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().titleEq("today").value();
        assertThat(book.publisher.observable().toBlocking().value().name, is("The Nova"));
    }

    @Test(expected = SQLiteException.class)
    public void onUpdateCascade() throws Exception {
        db.updateBook().publisher(SingleAssociation.<Publisher>id(0L)).execute();
    }

    @Test
    public void onDeleteCascade() throws Exception {
        db.deleteFromPublisher().execute();
        assertThat(db.selectFromBook().count(), is(0));
    }

    @Test
    public void onDeleteActions() throws Exception {
        ModelWithForeignKeyAction model = db.createModelWithForeignKeyAction(new ModelFactory<ModelWithForeignKeyAction>() {
            @NonNull
            @Override
            public ModelWithForeignKeyAction call() {
                ModelWithForeignKeyAction model = new ModelWithForeignKeyAction();
                model.author1 = db.createAuthor(new ModelFactory<Author>() {
                    @NonNull
                    @Override
                    public Author call() {
                        return Author.create("author1");
                    }
                });
                model.author2 = db.createAuthor(new ModelFactory<Author>() {
                    @NonNull
                    @Override
                    public Author call() {
                        return Author.create("author2");
                    }
                });
                model.author3 = db.createAuthor(new ModelFactory<Author>() {
                    @NonNull
                    @Override
                    public Author call() {
                        return Author.create("author3");
                    }
                });
                return model;
            }
        });

        Cursor cursor;

        // NO ACTION
        try {
            assert model.author1 != null;
            db.deleteFromAuthor().nameEq(model.author1.name).execute();
            fail();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(SQLiteConstraintException.class)));
        }

        // SET DEFAULT
        assert model.author2 != null;
        db.deleteFromAuthor().nameEq(model.author2.name).execute();
        cursor = db.selectFromModelWithForeignKeyAction().executeWithColumns(
                ModelWithForeignKeyAction_Schema.INSTANCE.author2.getQualifiedName());
        assertThat(getFirstStringAndClose(cursor), is(nullValue()));

        // CASCADE
        assert model.author3 != null;
        db.deleteFromAuthor().nameEq(model.author3.name).execute();
        assertThat(db.selectFromModelWithForeignKeyAction().count(), is(0));
    }

    private String getFirstStringAndClose(Cursor cursor) {
        cursor.moveToFirst();
        String result = cursor.getString(0);
        cursor.close();
        return result;
    }
}
