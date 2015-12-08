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
import com.github.gfx.android.orma.SingleRelation;
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
import android.support.annotation.NonNull;

import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class RxObservableTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaDatabase.builder(getContext()).name(null).build();

        final Publisher publisher = db.createPublisher(new ModelFactory<Publisher>() {
            @NonNull
            @Override
            public Publisher create() {
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
            public Book create() {
                Book book = new Book();
                book.title = "today";
                book.content = "milk, banana";
                book.inPrint = true;
                book.publisher = SingleRelation.id(publisher.id);
                return book;
            }
        });

        db.createBook(new ModelFactory<Book>() {
            @NonNull
            @Override
            public Book create() {
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
    public void relationObservable() throws Exception {
        List<Book> list = db.selectFromBook()
                .where("title = ?", "today")
                .observable()
                .toList()
                .toBlocking()
                .single();

        assertThat(list.size(), is(1));
        assertThat(list.get(0).title, is("today"));
    }

    @Test
    public void updaterObservable() throws Exception {
        int count = db.updateBook()
                .where("title = ?", "today")
                .content("modified")
                .observable()
                .toBlocking()
                .value();

        assertThat(count, is(1));
        assertThat(db.selectFromBook().where("title = ?", "today").value().content, is("modified"));
    }

    @Test
    public void deleterObservable() throws Exception {
        int count = db.deleteFromBook()
                .where("title = ?", "today")
                .observable()
                .toBlocking()
                .value();

        assertThat(count, is(1));
        assertThat(db.selectFromBook().where("title = ?", "today").valueOrNull(), is(nullValue()));
    }
}
