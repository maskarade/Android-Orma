package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.TransactionAbortException;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.example.orma.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

public class BookTest {

    OrmaDatabase db;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getOrma().resetDatabase();

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
        assertThat(db.fromTodo().count(), is(2L));
    }

    @Test
    public void toList() throws Exception {
        List<Book> books = db.fromTodo().toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        assertThat(books.get(1).title, is("friday"));
        assertThat(books.get(1).content, is("apple"));
    }

    @Test
    public void single() throws Exception {
        Book book = db.fromTodo().single();

        assert book != null;
        assertThat(book.title, is("today"));
        assertThat(book.content, is("milk, banana"));
    }

    @Test
    public void whereEquals() throws Exception {
        List<Book> books = db.fromTodo().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void whereLike() throws Exception {
        List<Book> books = db.fromTodo().where("title LIKE ?", "t%").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void orderBy() throws Exception {
        List<Book> books = db.fromTodo().orderBy("id DESC").toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(1).title, is("today"));
        assertThat(books.get(1).content, is("milk, banana"));

        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void limit() throws Exception {
        List<Book> books = db.fromTodo().limit(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void limitAndOffset() throws Exception {
        List<Book> books = db.fromTodo().limit(1).offset(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void offset() throws Exception {
        try {
            db.fromTodo().offset(1).toList();
            fail("not reached");
        } catch (Relation.InvalidStatementException e) {
            assertThat(e, is(notNullValue()));
        }
    }

    @Test
    public void delete() throws Exception {
        int result = db.fromTodo()
                .where("title = ?", "today")
                .delete();

        assertThat(result, is(1));
        assertThat(db.fromTodo().count(), is(1L));
        assertThat(db.fromTodo().single().title, is("friday"));
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

        assertThat(db.fromTodo().count(), is(7L));
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

        assertThat(db.fromTodo().count(), is(2L));
    }

}
