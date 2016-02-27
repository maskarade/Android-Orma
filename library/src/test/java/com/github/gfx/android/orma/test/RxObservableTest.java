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
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.functions.Func1;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class RxObservableTest {

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

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book call() {
                Book book = new Book();
                book.title = "tomorrow";
                book.content = "orange";
                book.inPrint = false;
                book.publisher = SingleAssociation.id(publisher.id);
                return book;
            }
        });
    }

    @Test
    public void selectorObservable() throws Exception {
        List<Book> list = db.selectFromBook()
                .titleEq("today")
                .executeAsObservable()
                .toList()
                .toBlocking()
                .single();

        assertThat(list.size(), is(1));
        assertThat(list.get(0).title, is("today"));
    }

    @Test
    public void inserterObservable() throws Exception {
        long rowid = db.prepareInsertIntoBook()
                .executeAsObservable(new ModelFactory<Book>() {
                    @NonNull
                    @Override
                    public Book call() {
                        Book book = new Book();
                        book.title = "observable days";
                        book.content = "reactive";
                        book.inPrint = false;
                        book.publisher = SingleAssociation.id(publisher.id);
                        return book;
                    }
                })
                .toBlocking()
                .value();
        assertThat(rowid, is(not(0L)));
        assertThat(db.selectFromBook().count(), is(3));
    }

    @Test
    public void updaterObservable() throws Exception {
        int count = db.updateBook()
                .titleEq("today")
                .content("modified")
                .executeAsObservable()
                .toBlocking()
                .value();

        assertThat(count, is(1));
        assertThat(db.selectFromBook().where("title = ?", "today").value().content, is("modified"));
    }

    @Test
    public void deleterObservable() throws Exception {
        int count = db.deleteFromBook()
                .titleEq("today")
                .executeAsObservable()
                .toBlocking()
                .value();

        assertThat(count, is(1));
        assertThat(db.selectFromBook().where("title = ?", "today").valueOrNull(), is(nullValue()));
    }

    @Test
    public void exceptionInObservable() throws Exception {
        final List<String> mapped = new ArrayList<>();
        final List<String> result = new ArrayList<>();

        db.selectFromBook()
                .executeAsObservable()
                .map(new Func1<Book, String>() {
                    @Override
                    public String call(Book book) {
                        mapped.add(book.title);
                        if (book.title.equals("friday")) {
                            throw new RuntimeException("died!");
                        }
                        return book.title;
                    }
                })
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        result.add("ON_COMPLETED");
                    }

                    @Override
                    public void onError(Throwable e) {
                        result.add(e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        result.add(s);
                    }
                });

        assertThat(mapped, contains("today", "friday"));
        assertThat(result, contains("today", "died!"));
    }

}
