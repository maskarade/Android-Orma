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
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.exception.InvalidStatementException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Author_Selector;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.Book_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.IteratorUtils;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class QueryTest {

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
                book.publisher = SingleAssociation.just(publisher);
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
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });
    }

    @Test
    public void count() throws Exception {
        assertThat(db.selectFromBook().count(), is(2));
    }

    @Test
    public void empty() throws Exception {
        assertThat(db.selectFromBook().isEmpty(), is(false));
    }

    @Test
    public void toList() throws Exception {
        List<Book> books = db.selectFromBook().toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        assertThat(books.get(1).title, is("friday"));
        assertThat(books.get(1).content, is("apple"));
    }

    @Test
    public void iterable() throws Exception {
        {
            List<Book> books = new ArrayList<>();

            for (Book book : db.selectFromBook().orderByTitleAsc()) {
                books.add(book);
            }

            assertThat(books, hasSize(2));

            assertThat(books.get(0).title, is("friday"));
            assertThat(books.get(1).title, is("today"));
        }
        {
            List<Book> books = new ArrayList<>();

            for (Book book : db.selectFromBook().orderByTitleDesc()) {
                books.add(book);
            }

            assertThat(books, hasSize(2));

            assertThat(books.get(0).title, is("today"));
            assertThat(books.get(1).title, is("friday"));
        }
    }

    @Test
    public void value() throws Exception {
        Book book = db.selectFromBook().value();

        assertThat(book.title, is("today"));
        assertThat(book.content, is("milk, banana"));
    }

    @Test
    public void valueOrNull() throws Exception {
        db.deleteFromBook().execute();
        Book book = db.selectFromBook().valueOrNull();

        assertThat(book, is(nullValue()));
    }

    @Test
    public void valueWithOffset() throws Exception {
        Book book = db.selectFromBook().offset(1).value();

        assertThat(book.title, is("friday"));
        assertThat(book.content, is("apple"));
    }

    @Test(expected = NoValueException.class)
    public void valueIfNull() throws Exception {
        db.deleteFromBook().execute();

        db.selectFromBook().value();
        fail("not reached");
    }

    @Test
    public void testGet() throws Exception {
        Book_Selector rel = db.selectFromBook();
        List<Book> books = rel.toList();
        assertThat(rel.get(0).bookId, is(books.get(0).bookId));
        assertThat(rel.get(1).bookId, is(books.get(1).bookId));
    }

    @Test(expected = NoValueException.class)
    public void testGetIfNoValue() throws Exception {
        db.selectFromBook().get(10);
    }

    @Test(expected = NoValueException.class)
    public void testGetOrNull() throws Exception {
        Book book = db.selectFromBook().getOrNull(0);
        assert book != null;
        assertThat(book.bookId, is(db.selectFromBook().get(0).bookId));
        assertThat(db.selectFromBook().get(10), is(nullValue()));
    }

    @Test
    public void testGetWithOffset() throws Exception {
        Book book = db.selectFromBook().offset(1).get(0);

        assertThat(book.title, is("friday"));
        assertThat(book.content, is("apple"));
    }

    @Test
    public void pluck() throws Exception {
        assertThat(db.selectFromBook().orderByTitleAsc().pluck(Book_Schema.INSTANCE.title),
                is(contains("friday", "today")));
        assertThat(db.selectFromBook().orderByTitleDesc().pluck(Book_Schema.INSTANCE.title),
                is(contains("today", "friday")));

        assertThat(db.selectFromBook().orderByTitleDesc().pluck(Book_Schema.INSTANCE.inPrint),
                is(contains(true, false)));
    }

    @Test
    public void aggregators() throws Exception {
        assertThat(db.selectFromBook().orderByTitleAsc().pluck(Book_Schema.INSTANCE.title),
                is(contains("friday", "today")));
        assertThat(db.selectFromBook().orderByTitleDesc().pluck(Book_Schema.INSTANCE.title),
                is(contains("today", "friday")));

        assertThat(db.selectFromBook().orderByTitleDesc().pluck(Book_Schema.INSTANCE.inPrint),
                is(contains(true, false)));
    }


    @Test
    public void execute() throws Exception {
        Cursor cursor = db.selectFromBook().execute();
        cursor.moveToFirst();

        assertThat(cursor.getCount(), is(2));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("title")), is("today"));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow("content")), is("milk, banana"));

        cursor.close();
    }

    @Test
    public void executeWithColumns() throws Exception {
        Cursor cursor = db.selectFromBook().executeWithColumns("max(bookId) as max_id, min(bookId) as min_id");
        cursor.moveToFirst();

        assertThat(cursor.getCount(), is(1));
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("max_id")), is(2L));
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow("min_id")), is(1L));

        cursor.close();
    }

    @Test
    public void newModelFromCursor() throws Exception {
        Cursor cursor = db.selectFromBook().execute();
        cursor.moveToFirst();

        Book book = db.newBookFromCursor(cursor);

        assertThat(book.title, is("today"));

        cursor.close();
    }

    @Test
    public void whereEquals() throws Exception {
        List<Book> books = db.selectFromBook().titleEq("today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void whereLike() throws Exception {
        List<Book> books = db.selectFromBook().where("title LIKE ?", "t%").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void whereConjunctionOr() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleEq("today")
                .or()
                .titleEq("friday")
                .toList();
        assertThat(books, hasSize(2));
    }

    @Test
    public void whereWithBoolean() throws Exception {
        List<Book> books = db.selectFromBook().where("inPrint = ?", true).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.selectFromBook().where("inPrint = ?", false).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void whereConjunctionAnd() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleEq("today")
                .and()
                .titleEq("friday")
                .toList();
        assertThat(books, hasSize(0));
    }

    @Test
    public void glob() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleGlob("*ri*")
                .toList();
        assertThat(books, hasSize(1));
    }

    @Test
    public void notGlob() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleNotGlob("*day")
                .toList();
        assertThat(books, hasSize(0));
    }

    @Test
    public void like() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleLike("%ri%")
                .toList();
        assertThat(books, hasSize(1));
    }

    @Test
    public void notLike() throws Exception {
        List<Book> books = db.selectFromBook()
                .titleNotLike("%day")
                .toList();
        assertThat(books, hasSize(0));
    }

    @Test
    public void orderBy() throws Exception {
        List<Book> books = db.selectFromBook().orderBy("bookId DESC").toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(1).title, is("today"));
        assertThat(books.get(1).content, is("milk, banana"));

        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void orderByTitleAsc() throws Exception {
        List<Book> books = db.selectFromBook().orderByTitleAsc().toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(1).title, is("today"));
        assertThat(books.get(1).content, is("milk, banana"));

        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void orderByTitleDesc() throws Exception {
        List<Book> books = db.selectFromBook().orderByTitleDesc().toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        assertThat(books.get(1).title, is("friday"));
        assertThat(books.get(1).content, is("apple"));
    }

    @Test
    public void limit() throws Exception {
        // toList
        {
            List<Book> books = db.selectFromBook().limit(1).toList();
            assertThat(books, hasSize(1));
            assertThat(books.get(0).title, is("today"));
            assertThat(books.get(0).content, is("milk, banana"));
            assertThat(db.selectFromBook().limit(2).toList(), hasSize(2));
        }

        // OrmaIterator
        {
            List<Book> books = IteratorUtils.listFromIterable(db.selectFromBook().limit(1));
            assertThat(books, hasSize(1));
            assertThat(books.get(0).title, is("today"));
            assertThat(books.get(0).content, is("milk, banana"));
            assertThat(db.selectFromBook().limit(2).toList(), hasSize(2));
        }
    }

    @Test
    public void limitAndOffset() throws Exception {
        for (int i = 0; i < 15; i++) {
            Book book = new Book();
            book.title = "name #" + i;
            book.content = "blah blah blah #" + i;
            book.publisher = SingleAssociation.just(publisher);

            db.insertIntoBook(book);
        }

        // toList
        {
            List<Book> books = db.selectFromBook().limit(3).offset(9).toList();
            assertThat(books, hasSize(3));
            assertThat(books.get(0).title, is("name #7"));
            assertThat(books.get(1).title, is("name #8"));
            assertThat(books.get(2).title, is("name #9"));
        }

        // OrmaIterator
        {
            List<Book> books = IteratorUtils.listFromIterable(db.selectFromBook().limit(3).offset(9));
            assertThat(books, hasSize(3));
            assertThat(books.get(0).title, is("name #7"));
            assertThat(books.get(1).title, is("name #8"));
            assertThat(books.get(2).title, is("name #9"));
        }
    }

    @Test
    public void pageAndPer() throws Exception {
        for (int i = 0; i < 5; i++) {
            Book book = new Book();
            book.title = "name #" + i;
            book.content = "blah blah blah #" + i;
            book.publisher = SingleAssociation.just(publisher);

            db.insertIntoBook(book);
        }

        // toList()
        {
            List<Book> books = db.selectFromBook().page(1).per(2).toList();
            assertThat(books, hasSize(2));
            assertThat(books.get(0).title, is("today"));
            assertThat(books.get(0).content, is("milk, banana"));

            assertThat(books.get(1).title, is("friday"));
            assertThat(books.get(1).content, is("apple"));

            books = db.selectFromBook().page(2).per(2).toList();
            assertThat(books, hasSize(2));
            assertThat(books.get(0).title, is("name #0"));
            assertThat(books.get(1).title, is("name #1"));
        }

        // OrmaIterator
        {
            List<Book> books = IteratorUtils.listFromIterable(db.selectFromBook().page(1).per(2));
            assertThat(books, hasSize(2));
            assertThat(books.get(0).title, is("today"));
            assertThat(books.get(0).content, is("milk, banana"));

            assertThat(books.get(1).title, is("friday"));
            assertThat(books.get(1).content, is("apple"));

            books = IteratorUtils.listFromIterable(db.selectFromBook().page(2).per(2));
            assertThat(books, hasSize(2));
            assertThat(books.get(0).title, is("name #0"));
            assertThat(books.get(1).title, is("name #1"));
        }
    }

    @Test
    public void offset() throws Exception {
        try {
            db.selectFromBook().offset(1).toList();
            fail("not reached");
        } catch (InvalidStatementException e) {
            assertThat(e, is(notNullValue()));
        }
    }

    @Test
    public void groupByAndHaving() throws Exception {
        publisher = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "foo bar baz";
                publisher.startedYear = 2009;
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
                book.content = "avocado";
                book.inPrint = true;
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "today";
                book.content = "avocado";
                book.inPrint = true;
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "friday";
                book.content = "fig";
                book.inPrint = false;
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });

        List<Book> books = db.selectFromBook().groupBy("title").having("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(db.selectFromBook().groupBy("title").count(), is(2));
    }

    @Test
    public void update() throws Exception {
        int count = db.updateBook()
                .titleEq("today")
                .content("modified")
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().titleEq("today").value();
        assertThat(book.content, is("modified"));
    }

    @Test
    public void updateViaRelation() throws Exception {
        int count = db.relationOfBook()
                .titleEq("today")
                .updater()
                .content("modified")
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().titleEq("today").value();
        assertThat(book.content, is("modified"));
    }

    @Test
    public void delete() throws Exception {
        int result = db.deleteFromBook()
                .titleEq("today")
                .execute();

        assertThat(result, is(1));
        assertThat(db.selectFromBook().count(), is(1));
        assertThat(db.selectFromBook().value().title, is("friday"));
    }

    @Test
    public void deleteViaRelation() throws Exception {
        int result = db.relationOfBook()
                .titleEq("today")
                .deleter()
                .execute();

        assertThat(result, is(1));
        assertThat(db.selectFromBook().count(), is(1));
        assertThat(db.selectFromBook().value().title, is("friday"));
    }

    public List<Book> someBooks() {
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Book book = new Book();
            book.title = "friday";
            book.content = "apple" + i;
            book.publisher = SingleAssociation.just(publisher);
            books.add(book);
        }
        return books;
    }

    @Test
    public void transactionAsCompleableWithSuccess() throws Exception {
        db.transactionAsCompletable(new Runnable() {
            @Override
            public void run() {
                db.prepareInsertIntoBook().executeAll(someBooks());
            }
        })
                .test()
                .assertComplete();

        assertThat(db.selectFromBook().count(), is(7));
    }

    @Test
    public void transactionAsCompletableWithFailure() throws Exception {
        db.transactionAsCompletable(new Runnable() {
            @Override
            public void run() {
                db.prepareInsertIntoBook().executeAll(someBooks());
                throw new RuntimeException("abort!");
            }
        })
                .test()
                .assertError(RuntimeException.class);

        assertThat(db.selectFromBook().count(), is(2));
    }

    @Test(expected = SQLiteException.class)
    public void insertOnConflict() throws Exception {
        Author author = new Author();
        author.name = "山田太郎";
        db.insertIntoAuthor(author);
        db.insertIntoAuthor(author);
    }

    @Test
    public void insertOrReplace() throws Exception {
        Author author1 = new Author();
        author1.name = "山田太郎";
        author1.note = "foo";
        long id1 = db.prepareInsertIntoAuthor(OnConflict.REPLACE).execute(author1);

        Author author2 = new Author();
        author2.name = "山田太郎";
        author2.note = "bar";
        long id2 = db.prepareInsertIntoAuthor(OnConflict.REPLACE).execute(author2);

        assertThat("The row id changes by INSERT OR REPLACE", id1, is(not(id2)));

        Author_Selector authors = db.selectFromAuthor().nameEq("山田太郎");
        assertThat(authors.count(), is(1));
        assertThat(authors.value().note, is("bar"));
    }

    @Test
    public void insertOrIgnore() throws Exception {
        Author author1 = new Author();
        author1.name = "山田太郎";
        author1.note = "foo";
        long id1 = db.prepareInsertIntoAuthor(OnConflict.IGNORE).execute(author1);
        assertThat(id1, is(not(0L)));

        Author author2 = new Author();
        author2.name = "山田太郎";
        author2.note = "bar";
        db.prepareInsertIntoAuthor(OnConflict.IGNORE).execute(author2);

        Author_Selector authors = db.selectFromAuthor().nameEq("山田太郎");
        assertThat(authors.count(), is(1));
        assertThat("INSERT is ignored!", authors.value().note, is("foo"));
    }

    @Test
    public void inserterExecuteAll() throws Exception {
        Inserter<Book> inserter = db.prepareInsertIntoBook();
        inserter.executeAll(someBooks());

        assertThat(db.selectFromBook().count(), is(7));
    }

    @Test
    public void inserterExecuteModelFactory() throws Exception {
        Inserter<Book> inserter = db.prepareInsertIntoBook();
        inserter.execute(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "monday";
                book.content = "apple";
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });

        assertThat(db.selectFromBook().count(), is(3));
    }


    @Test
    public void reuseCursor() throws Exception {
        List<Book> books = db.selectFromBook().titleEq("today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.selectFromBook().titleEq("friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));

        books = db.selectFromBook().titleEq("today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.selectFromBook().titleEq("friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void initAndInsertForSecondTable() throws Exception {
        db.deleteFromPublisher().execute();

        Inserter<Publisher> inserter = db.prepareInsertIntoPublisher();

        inserter.execute(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "The Fire";
                publisher.startedYear = 1998;
                publisher.startedMonth = 12;
                return publisher;
            }
        });

        inserter.execute(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher call() {
                Publisher publisher = new Publisher();
                publisher.name = "The Ice";
                publisher.startedYear = 2012;
                publisher.startedMonth = 6;
                return publisher;
            }
        });

        assertThat(db.selectFromPublisher().count(), is(2));

        Publisher publisher = db.selectFromPublisher().value();
        assertThat(publisher.name, is("The Fire"));
        assertThat(publisher.startedYear, is(1998));
        assertThat(publisher.startedMonth, is(12));
    }

    @Test
    public void rawQuery() throws Exception {
        Cursor cursor = db.getConnection().rawQuery("select count (*) from sqlite_master");
        assertThat(cursor.getCount(), greaterThan(0));
        cursor.close();
    }

    @Test
    public void rawQueryForSingleLong() throws Exception {
        long count = db.getConnection().rawQueryForLong("select count (*) from sqlite_master");
        assertThat(count, greaterThan(0L));
    }

    @Test
    public void execSQL() throws Exception {
        String name = "orma_test_executeSql";
        db.getConnection().execSQL("CREATE TABLE " + name + " (id integer primary key)");
        long value = db.getConnection().rawQueryForLong("select count(*) from sqlite_master where name = ?", name);
        assertThat(value, is(1L));
    }

}
