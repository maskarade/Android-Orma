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
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.widget.OrmaAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class OrmaAdapterTest {

    OrmaAdapter<Author> adapter;

    static Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        OrmaDatabase orma = OrmaBuilder.create();

        Inserter<Author> inserter = orma.prepareInsertIntoAuthor();
        inserter.execute(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "A";
                author.note = "foo";
                return author;
            }
        });

        inserter.execute(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "B";
                author.note = "bar";
                return author;
            }
        });

        inserter.execute(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "C";
                author.note = "baz";
                return author;
            }
        });

        adapter = new OrmaAdapter<>(getContext(), orma.relationOfAuthor().orderByNameAsc());
    }

    @Test
    public void testGetContext() throws Exception {
        assertThat(adapter.getContext(), is(instanceOf(Context.class)));
    }

    @Test
    public void testGetLayoutInflater() throws Exception {
        assertThat(adapter.getLayoutInflater(), is(instanceOf(LayoutInflater.class)));
    }

    @Test
    public void testGetItemCount() throws Exception {
        assertThat(adapter.getItemCount(), is(3));
    }

    @Test
    public void testGetRelation() throws Exception {
        assertThat(adapter.getRelation(), is(instanceOf(Relation.class)));
    }

    @Test
    public void testGetItem() throws Exception {
        assertThat(adapter.getItem(0).name, is("A"));
        assertThat(adapter.getItem(1).name, is("B"));
        assertThat(adapter.getItem(2).name, is("C"));
    }

    @Test
    public void testAddItemAsObservable() throws Exception {
        long id = adapter.addItemAsObservable(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "D";
                author.note = "new";
                return author;
            }
        }).toBlocking().value();
        assertThat(id, is(not(-1L)));
        assertThat(adapter.getItem(3).name, is("D"));
    }

    @Test
    public void testRemoveItemAsObservable() throws Exception {
        int deletedPosition = adapter.removeItemAsObservable(adapter.getItem(0))
                .toBlocking().single();
        assertThat(deletedPosition, is(0));
        assertThat(adapter.getItemCount(), is(2));
    }

    @Test
    public void testClearAsObservable() throws Exception {
        int deletedCount = adapter.clearAsObservable().toBlocking().value();
        assertThat(deletedCount, is(3));
    }
}
