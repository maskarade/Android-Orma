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

import com.github.gfx.android.orma.event.DataSetChangedEvent;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Author_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;
import com.github.gfx.android.orma.test.toolbox.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeFalse;

@RunWith(AndroidJUnit4.class)
public class QueryObservableTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        db.insertIntoAuthor(Author.create("foo"));
    }

    @Test
    public void example() throws Exception {
        final List<String> result = new ArrayList<>();

        Observable<Author_Selector> observable = db.relationOfAuthor()
                .createQueryObservable();
        Disposable subscription = observable.flatMap(new Function<Author_Selector, Observable<Author>>() {
            @Override
            public Observable<Author> apply(Author_Selector selector) throws Exception {
                return selector.executeAsObservable();
            }
        })
                .map(new Function<Author, String>() {
                    @Override
                    public String apply(Author author) throws Exception {
                        return author.name;
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        result.add(s);
                    }
                });

        // fire an event
        db.insertIntoAuthor(Author.create("bar"));
        assertThat(result, contains("foo", "bar"));

        // fire another event
        result.clear();
        db.insertIntoAuthor(Author.create("baz"));
        assertThat(result, contains("foo", "bar", "baz"));

        subscription.dispose();
    }

    @Test
    public void autoDispose() throws Exception {
        assumeFalse(TestUtils.runOnAndroid()); // FIXME

        final List<String> result = new ArrayList<>();

        Disposable subscription = db.relationOfAuthor()
                .<Author_Selector>createQueryObservable()
                .flatMap(new Function<Author_Selector, Observable<Author>>() {
                    @Override
                    public Observable<Author> apply(Author_Selector selector) throws Exception {
                        return selector.executeAsObservable();
                    }
                })
                .map(new Function<Author, String>() {
            @Override
            public String apply(Author author) throws Exception {
                return author.name;
            }
        })
                .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                result.add(s);
            }
        });
        subscription.dispose();

        System.gc();

        // fire an event, but the observer is disposed by GC
        db.insertIntoAuthor(Author.create("bar"));
        assertThat(result, hasSize(0));
    }

    @SuppressWarnings({"deprecation"})
    @Test
    public void eventTypes() throws Exception {
        final List<DataSetChangedEvent.Type> result = new ArrayList<>();

        Observable<DataSetChangedEvent<Author_Selector>> observable = db.relationOfAuthor()
                .createEventObservable();
        observable.map(new Function<DataSetChangedEvent<Author_Selector>, DataSetChangedEvent.Type>() {
            @Override
            public DataSetChangedEvent.Type apply(DataSetChangedEvent<Author_Selector> event) throws Exception {
                return event.getType();
            }
        }).subscribe(new Consumer<DataSetChangedEvent.Type>() {
            @Override
            public void accept(DataSetChangedEvent.Type type) throws Exception {
                result.add(type);
            }
        });

        // fire an INSERT event
        db.insertIntoAuthor(Author.create("bar"));
        assertThat(result, contains(DataSetChangedEvent.Type.INSERT));

        // fire an UPDATE event
        result.clear();
        db.updateAuthor().note("test note").execute();
        assertThat(result, contains(DataSetChangedEvent.Type.UPDATE));

        // fire an DELETE event
        result.clear();
        db.deleteFromAuthor().execute();
        assertThat(result, contains(DataSetChangedEvent.Type.DELETE));

        // fire an TRANSACTION
        result.clear();
        db.transactionSync(new Runnable() {
            @Override
            public void run() {
                db.insertIntoAuthor(Author.create("foo"));
                db.insertIntoAuthor(Author.create("bar"));
                db.insertIntoAuthor(Author.create("baz"));
            }
        });
        assertThat(result, hasSize(1));
        assertThat(result, contains(DataSetChangedEvent.Type.TRANSACTION));

        result.clear();
        db.transactionNonExclusiveSync(new Runnable() {
            @Override
            public void run() {
                db.insertIntoAuthor(Author.create("FOO"));
                db.insertIntoAuthor(Author.create("BAR"));
                db.insertIntoAuthor(Author.create("BAZ"));
            }
        });
        assertThat(result, hasSize(1));
        assertThat(result, contains(DataSetChangedEvent.Type.TRANSACTION));
    }

}
