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

import io.reactivex.functions.Function;

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

    Book_Selector selector() {
        return db.selectFromBook().titleNotEq("tomorrow");
    }

    @Test
    public void countAsSingle2() throws Exception {
        selector().countAsSingle2().test().assertResult(2);
    }

    @Test
    public void countAsObservable2() throws Exception {
        selector().countAsObservable2().test().assertResult(2);
    }

    @Test
    public void selectorObservable2() throws Exception {
        selector().titleEq("today")
                .executeAsObservable2()
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
                .pluckAsObservable2(Book_Schema.INSTANCE.title)
                .test()
                .assertResult("friday", "today");

        selector().orderByTitleDesc()
                .pluckAsObservable2(Book_Schema.INSTANCE.title)
                .test()
                .assertResult("today", "friday");

        selector().orderByTitleAsc()
                .pluckAsObservable2(Book_Schema.INSTANCE.inPrint)
                .test()
                .assertResult(false, true);

        selector().orderByTitleDesc()
                .pluckAsObservable2(Book_Schema.INSTANCE.inPrint)
                .test()
                .assertResult(true, false);

    }
}
