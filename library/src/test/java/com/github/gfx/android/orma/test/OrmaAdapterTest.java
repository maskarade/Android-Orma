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
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;
import com.github.gfx.android.orma.widget.OrmaAdapter;
import com.github.gfx.android.orma.widget.OrmaListAdapter;
import com.github.gfx.android.orma.widget.OrmaRecyclerViewAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.functions.Predicate;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class OrmaAdapterTest {

    OrmaDatabase db;

    OrmaAdapter<Author> adapter;

    static Context getContext() {
        return ApplicationProvider.getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        Inserter<Author> inserter = db.prepareInsertIntoAuthor();
        inserter.execute(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "A";
                author.note = "foo";
                return author;
            }
        });

        inserter.execute(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "B";
                author.note = "bar";
                return author;
            }
        });

        inserter.execute(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "C";
                author.note = "baz";
                return author;
            }
        });

        inserter.execute(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "Z";
                return author;
            }
        });

        adapter = new OrmaAdapter<>(getContext(),
                db.relationOfAuthor().noteIsNotNull().orderByNameAsc());
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
    public void testGetItemAsObservable() throws Exception {
        assertThat(adapter.getItemAsSingle(0).blockingGet().name, is("A"));
        assertThat(adapter.getItemAsSingle(1).blockingGet().name, is("B"));
        assertThat(adapter.getItemAsSingle(2).blockingGet().name, is("C"));
    }

    @Test
    public void testAddItemAsObservable() throws Exception {
        adapter.addItemAsSingle(new ModelFactory<Author>() {
            @NonNull
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "D";
                author.note = "new";
                return author;
            }
        })
                .test()
                .assertValue(new Predicate<Long>() {
                    @Override
                    public boolean test(Long rowId) throws Exception {
                        return rowId > 0;
                    }
                })
                .assertComplete();
        assertThat(adapter.getItem(3).name, is("D"));
    }

    @Test
    public void testUpdateAndGetItem() throws Exception {
        db.updateAuthor()
                .nameEq("A")
                .note("foo/bar/baz")
                .execute();

        assertThat(adapter.getItem(0).name, is("A"));
        assertThat(adapter.getItem(0).note, is("foo/bar/baz"));
    }

    @Test
    public void testRemoveItemAsObservable() throws Exception {
        adapter.removeItemAsMaybe(adapter.getItem(0))
                .test()
                .assertResult(0);
        assertThat(adapter.getItemCount(), is(2));
    }

    @Test
    public void testClearAsObservable() throws Exception {
        adapter.clearAsSingle()
                .test()
                .assertResult(3);
        assertThat(adapter.getItemCount(), is(0));
    }

    @Test
    public void explicitNotifyDataSetChangedForListViewAdapter() throws Exception {
        OrmaListAdapter<Author> listAdapter = new OrmaListAdapter<Author>(adapter) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return null;
            }
        };

        db.updateAuthor()
                .nameEq("A")
                .note("foo/bar/baz")
                .execute();

        assertThat(listAdapter.getItem(0).name, is("A"));
        assertThat(listAdapter.getItem(0).note, is("foo/bar/baz"));
    }

    @Test
    public void explicitNotifyDataSetChangedForRecyclerViewAdapter() throws Exception {
        class VH extends RecyclerView.ViewHolder {

            public VH(View itemView) {
                super(itemView);
            }
        }

        OrmaRecyclerViewAdapter<Author, VH> listAdapter = new OrmaRecyclerViewAdapter<Author, VH>(adapter) {
            @Override
            public VH onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(VH holder, int position) {

            }
        };

        listAdapter.getItem(0); // to touch the cache

        db.updateAuthor()
                .nameEq("A")
                .note("foo/bar/baz")
                .execute();

        assertThat(listAdapter.getItem(0).name, is("A"));
        assertThat(listAdapter.getItem(0).note, is("foo/bar/baz"));
    }
}
