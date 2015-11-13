package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.TransactionAbortException;
import com.github.gfx.android.orma.TransactionTask;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class RelationTest {

    OrmaDatabase db;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getConnection().resetDatabase();

        {
            Book book = new Book();
            book.title = "today";
            book.content = "milk, banana";
            db.insert(book);
        }

        {
            Book book = new Book();
            book.title = "friday";
            book.content = "apple";
            db.insert(book);
        }
    }

    @Test
    public void count() throws Exception {
        assertThat(db.fromBook().count(), is(2L));
    }

    @Test
    public void toList() throws Exception {
        List<Book> books = db.fromBook().toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        assertThat(books.get(1).title, is("friday"));
        assertThat(books.get(1).content, is("apple"));
    }

    @Test
    public void single() throws Exception {
        Book book = db.fromBook().single();

        assertThat(book.title, is("today"));
        assertThat(book.content, is("milk, banana"));
    }

    @Test
    public void singleOrNull() throws Exception {
        db.fromBook().delete();
        Book book = db.fromBook().singleOrNull();

        assertThat(book, is(nullValue()));
    }

    @Test
    public void whereEquals() throws Exception {
        List<Book> books = db.fromBook().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void whereLike() throws Exception {
        List<Book> books = db.fromBook().where("title LIKE ?", "t%").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void orderBy() throws Exception {
        List<Book> books = db.fromBook().orderBy("id DESC").toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(1).title, is("today"));
        assertThat(books.get(1).content, is("milk, banana"));

        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void limit() throws Exception {
        List<Book> books = db.fromBook().limit(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void limitAndOffset() throws Exception {
        List<Book> books = db.fromBook().limit(1).offset(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void offset() throws Exception {
        try {
            db.fromBook().offset(1).toList();
            fail("not reached");
        } catch (Relation.InvalidStatementException e) {
            assertThat(e, is(notNullValue()));
        }
    }

    @Test
    public void delete() throws Exception {
        int result = db.fromBook()
                .where("title = ?", "today")
                .delete();

        assertThat(result, is(1));
        assertThat(db.fromBook().count(), is(1L));
        assertThat(db.fromBook().single().title, is("friday"));
    }

    @Test
    public void transactionSuccess() throws Exception {
        db.transaction(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                for (int i = 0; i < 5; i++) {
                    Book book = new Book();
                    book.title = "friday";
                    book.content = "apple" + i;
                    db.insert(book);
                }
            }
        });

        assertThat(db.fromBook().count(), is(7L));
    }

    @Test
    public void transactionAbort() throws Exception {
        try {
            db.transaction(new TransactionTask() {
                @Override
                public void execute() throws Exception {
                    for (int i = 0; i < 5; i++) {
                        Book book = new Book();
                        book.title = "friday";
                        book.content = "apple" + i;
                        db.insert(book);
                    }
                    throw new RuntimeException("abort!");
                }
            });
            fail("not reached");
        } catch (TransactionAbortException e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
        }

        assertThat(db.fromBook().count(), is(2L));
    }

    @Test
    public void reuseCursor() throws Exception {
        List<Book> books = db.fromBook().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.fromBook().where("title = ?", "friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));

        books = db.fromBook().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.fromBook().where("title = ?", "friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void initAndInsertForSecondTable() throws Exception {
        {
            Publisher publisher = new Publisher();
            publisher.name = "The Fire";
            publisher.startedYear = 1998;
            publisher.startedMonth = 12;
            db.insert(publisher);
        }

        {
            Publisher publisher = new Publisher();
            publisher.name = "The Ice";
            publisher.startedYear = 2012;
            publisher.startedMonth = 6;
            db.insert(publisher);
        }

        assertThat(db.fromPublisher().count(), is(2L));

        Publisher publisher = db.fromPublisher().single();
        assertThat(publisher.name, is("The Fire"));
        assertThat(publisher.startedYear, is(1998));
        assertThat(publisher.startedMonth, is(12));
    }
}
