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
import com.github.gfx.android.orma.test.model.Author_Relation;
import com.github.gfx.android.orma.test.model.ModelWithMultipleSortableColumns;
import com.github.gfx.android.orma.test.model.ModelWithMultipleSortableColumns_Relation;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class RelationTest {

    OrmaDatabase orma;

    @Before
    public void setUp() throws Exception {
        orma = OrmaFactory.create();

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

        inserter.execute(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "D";
                author.note = "nobody";
                return author;
            }
        });
    }

    Author_Relation rel() {
        return orma.relationOfAuthor().nameNotEq("D");
    }

    Author find(String name) {
        return orma.selectFromAuthor().nameEq(name).value();
    }

    @Test
    public void indexOf_in_asc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameAsc();
        assertThat(rel.indexOf(find("A")), is(0));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(2));
    }

    @Test
    public void indexOf_in_desc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameDesc();
        assertThat(rel.indexOf(find("A")), is(2));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(0));
    }

    @Test
    public void get_in_asc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameAsc();
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("C"));
    }

    @Test
    public void get_in_desc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameDesc();
        assertThat(rel.get(2).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(0).name, is("C"));
    }

    @Test
    public void truncate_asc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameAsc();
        int deletedRows = rel.truncateAsObservable(2)
                .toBlocking()
                .value();

        assertThat(deletedRows, is(1));
        assertThat(rel.count(), is(2));
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
    }

    @Test
    public void truncate_asc_overflow() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameAsc();
        int deletedRows = rel.truncateAsObservable(10)
                .toBlocking()
                .value();

        assertThat(deletedRows, is(0));
        assertThat(rel.count(), is(3));
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("C"));
    }

    @Test
    public void truncate_desc() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameDesc();
        int deletedRows = rel.truncateAsObservable(10)
                .toBlocking()
                .value();

        assertThat(deletedRows, is(0));
        assertThat(rel.count(), is(3));
        assertThat(rel.get(0).name, is("C"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("A"));
    }

    @Test
    public void testMultipleOrderingTerms() throws Exception {
        orma.createModelWithMultipleSortableColumns(new ModelFactory<ModelWithMultipleSortableColumns>() {
            @Override
            public ModelWithMultipleSortableColumns call() {
                ModelWithMultipleSortableColumns m = new ModelWithMultipleSortableColumns();
                m.id = 100;
                m.first = 10;
                m.second = 20;
                return m;
            }
        });

        orma.createModelWithMultipleSortableColumns(new ModelFactory<ModelWithMultipleSortableColumns>() {
            @Override
            public ModelWithMultipleSortableColumns call() {
                ModelWithMultipleSortableColumns m = new ModelWithMultipleSortableColumns();
                m.id = 200;
                m.first = 10;
                m.second = 30;
                return m;
            }
        });

        orma.createModelWithMultipleSortableColumns(new ModelFactory<ModelWithMultipleSortableColumns>() {
            @Override
            public ModelWithMultipleSortableColumns call() {
                ModelWithMultipleSortableColumns m = new ModelWithMultipleSortableColumns();
                m.id = 300;
                m.first = 5;
                m.second = 40;
                return m;
            }
        });

        ModelWithMultipleSortableColumns_Relation rel;
        rel = orma.relationOfModelWithMultipleSortableColumns()
                .orderByFirstAsc()
                .orderBySecondDesc();

        assertThat(rel.get(0).id, is(300L));
        assertThat(rel.get(1).id, is(200L));
        assertThat(rel.get(2).id, is(100L));

        rel = orma.relationOfModelWithMultipleSortableColumns()
                .orderByFirstDesc()
                .orderBySecondDesc();

        assertThat(rel.get(0).id, is(200L));
        assertThat(rel.get(1).id, is(100L));
        assertThat(rel.get(2).id, is(300L));
    }

    @Test
    public void iterable() throws Exception {
        Relation<Author, ?> rel = rel().orderByNameAsc();

        int count = 0;
        for (Author author : rel) {
            assertThat(author, is(notNullValue()));
            count++;
        }
        assertThat(count, is(3));

        count = 0;
        for (Author author : rel) {
            assertThat(author, is(notNullValue()));
            count++;
        }
        assertThat(count, is(3));
    }
}
