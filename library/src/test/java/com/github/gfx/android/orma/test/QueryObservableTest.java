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
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.Author_Relation;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class QueryObservableTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        db.createAuthor(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                return Author.create("foo");
            }
        });
    }

    @Test
    public void triggerByInsert() throws Exception {
        final List<String> result = new ArrayList<>();

        Observable<Author_Relation> observable = db.relationOfAuthor().createQueryObservable();
        observable.flatMap(new Function<Author_Relation, Observable<Author>>() {
            @Override
            public Observable<Author> apply(Author_Relation authors) throws Exception {
                return authors.selector().executeAsObservable();
            }
        }).map(new Function<Author, String>() {
            @Override
            public String apply(Author author) throws Exception {
                return author.name;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                result.add(s);
            }
        });

        // trigger an event
        db.createAuthor(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                return Author.create("bar");
            }
        });
        assertThat(result, contains("foo", "bar"));

        // trigger another event
        result.clear();
        db.createAuthor(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                return Author.create("baz");
            }
        });
        assertThat(result, contains("foo", "bar", "baz"));
    }
}
