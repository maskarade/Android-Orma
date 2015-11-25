package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ModelBuilder;
import com.github.gfx.android.orma.SingleRelation;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.exception.InvalidStatementException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.exception.TransactionAbortException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class RelationTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), null);

        final Publisher publisher = db.createPublisher(new ModelBuilder<Publisher>() {
            @Override
            public Publisher build() {
                Publisher publisher = new Publisher();
                publisher.name = "foo bar";
                publisher.startedYear = 2015;
                publisher.startedMonth = 12;

                return publisher;
            }
        });

        db.createBook(new ModelBuilder<Book>() {
            @Override
            public Book build() {
                Book book = new Book();
                book.title = "today";
                book.content = "milk, banana";
                book.inPrint = true;
                book.publisher = SingleRelation.id(publisher.id);
                return book;
            }
        });

        db.createBook(new ModelBuilder<Book>() {
            @Override
            public Book build() {
                Book book = new Book();
                book.title = "friday";
                book.content = "apple";
                book.inPrint = false;
                book.publisher = SingleRelation.id(publisher.id);
                return book;
            }
        });
    }

    @Test
    public void count() throws Exception {
        assertThat(db.selectFromBook().count(), is(2));
    }

    @Test
    public void countAsObservable() throws Exception {
        assertThat(db.selectFromBook().countAsObservable().toBlocking().single(), is(2));
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
    public void forEach() throws Exception {
        final List<Book> books = new ArrayList<>();

        db.selectFromBook().forEach(new Action1<Book>() {
            @Override
            public void call(Book book) {
                books.add(book);
            }
        });

        assertThat(books, hasSize(2));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        assertThat(books.get(1).title, is("friday"));
        assertThat(books.get(1).content, is("apple"));
    }


    @Test
    public void single() throws Exception {
        Book book = db.selectFromBook().value();

        assertThat(book.title, is("today"));
        assertThat(book.content, is("milk, banana"));
    }

    @Test
    public void singleOrNull() throws Exception {
        db.deleteFromBook().execute();
        Book book = db.selectFromBook().valueOrNull();

        assertThat(book, is(nullValue()));
    }

    @Test
    public void singleIfNull() throws Exception {
        db.deleteFromBook().execute();
        try {
            db.selectFromBook().value();
            fail("not reached");
        } catch (Exception e) {
            assertThat(e, is(instanceOf(NoValueException.class)));
        }
    }

    @Test
    public void whereEquals() throws Exception {
        List<Book> books = db.selectFromBook().where("title = ?", "today").toList();
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
                .where("title is ?", "today")
                .or()
                .where("title is ?", "friday")
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
                .where("title is ?", "today")
                .and()
                .where("title is ?", "friday")
                .toList();
        assertThat(books, hasSize(0));
    }

    @Test
    public void orderBy() throws Exception {
        List<Book> books = db.selectFromBook().orderBy("id DESC").toList();
        assertThat(books, hasSize(2));
        assertThat(books.get(1).title, is("today"));
        assertThat(books.get(1).content, is("milk, banana"));

        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void limit() throws Exception {
        List<Book> books = db.selectFromBook().limit(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));
    }

    @Test
    public void limitAndOffset() throws Exception {
        List<Book> books = db.selectFromBook().limit(1).offset(1).toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
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
    public void update() throws Exception {
        int count = db.updateBook()
                .where("title = ?", "today")
                .content("modified")
                .execute();

        assertThat(count, is(1));

        Book book = db.selectFromBook().where("title = ?", "today").value();
        assertThat(book.content, is("modified"));
    }

    @Test
    public void delete() throws Exception {
        int result = db.deleteFromBook()
                .where("title = ?", "today")
                .execute();

        assertThat(result, is(1));
        assertThat(db.selectFromBook().count(), is(1));
        assertThat(db.selectFromBook().value().title, is("friday"));
    }

    @Test
    public void transactionSyncSuccess() throws Exception {
        db.transactionSync(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                Publisher publisher = db.selectFromPublisher().value();

                for (int i = 0; i < 5; i++) {
                    Book book = new Book();
                    book.title = "friday";
                    book.content = "apple" + i;
                    book.publisher = SingleRelation.id(publisher.id);
                    db.insertIntoBook(book);
                }
            }
        });

        assertThat(db.selectFromBook().count(), is(7));
    }

    @Test
    public void transactionSyncAbort() throws Exception {
        try {
            db.transactionSync(new TransactionTask() {
                @Override
                public void execute() throws Exception {
                    for (int i = 0; i < 5; i++) {
                        Book book = new Book();
                        book.title = "friday";
                        book.content = "apple" + i;
                        db.insertIntoBook(book);
                    }
                    throw new Exception("abort!");
                }
            });
            fail("not reached");
        } catch (TransactionAbortException e) {
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
        }

        assertThat(db.selectFromBook().count(), is(2));
    }

    @Test
    public void transactionAsyncSuccess() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        db.transactionAsync(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                Publisher publisher = db.selectFromPublisher().value();

                for (int i = 0; i < 5; i++) {
                    Book book = new Book();
                    book.title = "friday";
                    book.content = "apple" + i;
                    book.publisher = SingleRelation.id(publisher.id);
                    db.insertIntoBook(book);
                }

                latch.countDown();
            }
        });

        assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
        assertThat(db.selectFromBook().count(), is(7));
    }

    @Test
    public void transactionAsyncAbort() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        db.transactionAsync(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                for (int i = 0; i < 5; i++) {
                    Book book = new Book();
                    book.title = "friday";
                    book.content = "apple" + i;
                    db.insertIntoBook(book);
                }
                throw new Exception("abort!");
            }

            @Override
            public void onError(Exception exception) {
                assertThat(exception, is(instanceOf(RuntimeException.class)));
                latch.countDown();
            }
        });

        assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
        assertThat(db.selectFromBook().count(), is(2));
    }

    @Test
    public void reuseCursor() throws Exception {
        List<Book> books = db.selectFromBook().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.selectFromBook().where("title = ?", "friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));

        books = db.selectFromBook().where("title = ?", "today").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("today"));
        assertThat(books.get(0).content, is("milk, banana"));

        books = db.selectFromBook().where("title = ?", "friday").toList();
        assertThat(books, hasSize(1));
        assertThat(books.get(0).title, is("friday"));
        assertThat(books.get(0).content, is("apple"));
    }

    @Test
    public void initAndInsertForSecondTable() throws Exception {
        db.deleteFromPublisher().execute();

        {
            Publisher publisher = new Publisher();
            publisher.name = "The Fire";
            publisher.startedYear = 1998;
            publisher.startedMonth = 12;
            db.insertIntoPublisher(publisher);
        }

        {
            Publisher publisher = new Publisher();
            publisher.name = "The Ice";
            publisher.startedYear = 2012;
            publisher.startedMonth = 6;
            db.insertIntoPublisher(publisher);
        }

        assertThat(db.selectFromPublisher().count(), is(2));

        Publisher publisher = db.selectFromPublisher().value();
        assertThat(publisher.name, is("The Fire"));
        assertThat(publisher.startedYear, is(1998));
        assertThat(publisher.startedMonth, is(12));
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
        final Publisher a = db.createPublisher(new ModelBuilder<Publisher>() {
            @Override
            public Publisher build() {
                Publisher publisher = new Publisher();
                publisher.name = "A";
                return publisher;
            }
        });

        final Publisher b = db.createPublisher(new ModelBuilder<Publisher>() {
            @Override
            public Publisher build() {
                Publisher publisher = new Publisher();
                publisher.name = "B";
                return publisher;
            }
        });

        for (int i = 0; i < 2; i++) {
            final int x = i;
            db.createBook(new ModelBuilder<Book>() {
                @Override
                public Book build() {
                    Book book = new Book();
                    book.publisher = SingleRelation.id(a.id);
                    book.title = "a " + x;
                    return book;
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            final int x = i;

            db.createBook(new ModelBuilder<Book>() {
                @Override
                public Book build() {
                    Book book = new Book();
                    book.publisher = SingleRelation.id(b.id);
                    book.title = "b " + x;
                    return book;
                }
            });
        }

        assertThat(a.books(db).count(), is(2));
        assertThat(b.books(db).count(), is(3));
    }
}
