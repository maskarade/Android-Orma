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
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.Book_Schema;
import com.github.gfx.android.orma.test.model.Book_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.ObservableSource;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class RxJava2Test {

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

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "tomorrow";
                book.content = "orange";
                book.inPrint = false;
                book.publisher = SingleAssociation.just(publisher);
                return book;
            }
        });
    }

    Book_Selector selector() {
        return db.selectFromBook().titleNotEq("tomorrow");
    }

    @Test
    public void countAsSingle2() throws Exception {
        selector().countAsSingle()
                .test()
                .assertResult(2);
    }

    @Test
    public void selectorObservable2() throws Exception {
        selector().titleEq("today")
                .executeAsObservable()
                .map(new Function<Book, String>() {
                    @Override
                    public String apply(Book book) throws Exception {
                        return book.title;
                    }
                })
                .test()
                .assertResult("today");
    }

    @Test
    public void pluckAsObservable2() throws Exception {
        selector().orderByTitleAsc()
                .pluckAsObservable(Book_Schema.INSTANCE.title)
                .test()
                .assertResult("friday", "today");

        selector().orderByTitleDesc()
                .pluckAsObservable(Book_Schema.INSTANCE.title)
                .test()
                .assertResult("today", "friday");

        selector().orderByTitleAsc()
                .pluckAsObservable(Book_Schema.INSTANCE.inPrint)
                .test()
                .assertResult(false, true);

        selector().orderByTitleDesc()
                .pluckAsObservable(Book_Schema.INSTANCE.inPrint)
                .test()
                .assertResult(true, false);

    }

    @Test
    public void inserterSingle2() throws Exception {
        db.prepareInsertIntoBookAsSingle()
                .flatMap(new Function<Inserter<Book>, SingleSource<Long>>() {
                    @Override
                    public SingleSource<Long> apply(Inserter<Book> bookInserter) throws Exception {
                        return bookInserter.executeAsSingle(new Callable<Book>() {
                            @NonNull
                            @Override
                            public Book call() {
                                Book book = new Book();
                                book.title = "single days";
                                book.content = "reactive";
                                book.inPrint = false;
                                book.publisher = SingleAssociation.just(publisher);
                                return book;
                            }
                        });
                    }
                })
                .test()
                .assertValue(new Predicate<Long>() {
                    @Override
                    public boolean test(Long rowId) throws Exception {
                        return rowId != 0L;
                    }
                });

        assertThat(db.selectFromBook().count(), is(4));
    }

    @Test
    public void inserterInsertAllAsSingle2() throws Exception {
        db.prepareInsertIntoBookAsSingle()
                .flatMapObservable(new Function<Inserter<Book>, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Inserter<Book> bookInserter) throws Exception {
                        Book book = new Book();
                        book.title = "single days";
                        book.content = "reactive";
                        book.inPrint = false;
                        book.publisher = SingleAssociation.just(publisher);
                        return bookInserter.executeAllAsObservable(Collections.singleton(book));
                    }
                })
                .test()
                .assertValue(new Predicate<Long>() {
                    @Override
                    public boolean test(Long rowId) throws Exception {
                        return rowId != 0L;
                    }
                });

        assertThat(db.selectFromBook().count(), is(4));
    }

    @Test
    public void updateSingle2() throws Exception {
        db.updateBook()
                .titleEq("today")
                .content("modified")
                .executeAsSingle()
                .test()
                .assertResult(1);

        assertThat(db.selectFromBook().titleEq("today").value().content, is("modified"));
    }

    @Test
    public void deleteSingle2() throws Exception {
        db.deleteFromBook()
                .titleEq("today")
                .executeAsSingle()
                .test()
                .assertResult(1);

        assertThat(db.selectFromBook().titleEq("today").valueOrNull(), is(nullValue()));
    }

    @Test
    public void exceptionInObservable2() throws Exception {
        @SuppressWarnings("serial")
        class AbortInMapException extends RuntimeException {

        }

        final List<String> mapped = new ArrayList<>();

        db.selectFromBook()
                .executeAsObservable()
                .map(new Function<Book, String>() {
                    @Override
                    public String apply(Book book) throws Exception {
                        mapped.add(book.title);
                        if ("friday".equals(book.title)) {
                            throw new AbortInMapException();
                        }
                        return book.title;
                    }
                })
                .test()
                .assertValues("today")
                .assertNotComplete()
                .assertError(AbortInMapException.class);

        assertThat(mapped, contains("today", "friday"));
    }
}
