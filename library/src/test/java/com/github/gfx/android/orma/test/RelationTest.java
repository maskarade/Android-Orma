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

import com.github.gfx.android.orma.BuiltInSerializers;
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.rx.RxRelation;
import com.github.gfx.android.orma.test.model.ModelWithDate;
import com.github.gfx.android.orma.test.model.ModelWithDate_Relation;
import com.github.gfx.android.orma.test.model.ModelWithMultipleSortableColumns;
import com.github.gfx.android.orma.test.model.ModelWithMultipleSortableColumns_Relation;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentValues;

import java.util.Arrays;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class RelationTest {

    OrmaDatabase orma;

    @Before
    public void setUp() throws Exception {
        orma = OrmaFactory.create();

        final long t = System.currentTimeMillis();

        orma.transactionSync(new Runnable() {
            @Override
            public void run() {
                Inserter<ModelWithDate> inserter = orma.prepareInsertIntoModelWithDate();
                inserter.execute(new ModelFactory<ModelWithDate>() {
                    @NonNull
                    @Override
                    public ModelWithDate call() {
                        ModelWithDate model = new ModelWithDate();
                        model.name = "A";
                        model.note = "foo";
                        model.time = new Date(t);
                        return model;
                    }
                });

                inserter.execute(new ModelFactory<ModelWithDate>() {
                    @NonNull
                    @Override
                    public ModelWithDate call() {
                        ModelWithDate model = new ModelWithDate();
                        model.name = "B";
                        model.note = "bar";
                        model.time = new Date(t + 1);
                        return model;
                    }
                });

                inserter.execute(new ModelFactory<ModelWithDate>() {
                    @NonNull
                    @Override
                    public ModelWithDate call() {
                        ModelWithDate model = new ModelWithDate();
                        model.name = "C";
                        model.note = "baz";
                        model.time = new Date(t + 2);
                        return model;
                    }
                });

                inserter.execute(new ModelFactory<ModelWithDate>() {
                    @NonNull
                    @Override
                    public ModelWithDate call() {
                        ModelWithDate model = new ModelWithDate();
                        model.name = "D";
                        model.note = "nobody";
                        model.time = new Date(t + 3);
                        return model;
                    }
                });
            }
        });
    }

    ModelWithDate_Relation rel() {
        return orma.relationOfModelWithDate().nameNotEq("D");
    }

    ModelWithDate find(String name) {
        return orma.selectFromModelWithDate().nameEq(name).value();
    }

    @Test
    public void count() throws Exception {
        assertThat(rel().count(), is(3));
    }

    @Test
    public void isEmpty() throws Exception {
        assertThat(rel().isEmpty(), is(false));
    }

    @Test
    public void indexOfInAsc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();
        assertThat(rel.indexOf(find("A")), is(0));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(2));
    }

    @Test
    public void indexOfInDesc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameDesc();
        assertThat(rel.indexOf(find("A")), is(2));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(0));
    }

    @Test
    public void indexOfInAscForDate() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByTimeAsc();
        assertThat(rel.indexOf(find("A")), is(0));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(2));
    }

    @Test
    public void indexOfInDescForDate() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByTimeDesc();
        assertThat(rel.indexOf(find("A")), is(2));
        assertThat(rel.indexOf(find("B")), is(1));
        assertThat(rel.indexOf(find("C")), is(0));
    }

    @Test
    public void getInAsc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("C"));
    }

    @Test
    public void getInDesc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameDesc();
        assertThat(rel.get(2).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(0).name, is("C"));
    }

    @Test
    public void truncateAsc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();
        rel.truncateAsSingle(2)
                .test()
                .assertResult(1);

        assertThat(rel.count(), is(2));
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
    }

    @Test
    public void truncateAscOverflow() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();
        rel.truncateAsSingle(10)
                .test()
                .assertResult(0);

        assertThat(rel.count(), is(3));
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("C"));
    }

    @Test
    public void truncateDesc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameDesc();
        rel.truncateAsSingle(10)
                .test()
                .assertResult(0);

        assertThat(rel.count(), is(3));
        assertThat(rel.get(0).name, is("C"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("A"));
    }

    @Test
    public void deleteAsObservableAsc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();

        rel.deleteAsMaybe(rel.get(2))
                .test()
                .assertResult(2);

        assertThat(rel.count(), is(2));
        assertThat(rel.get(0).name, is("A"));
        assertThat(rel.get(1).name, is("B"));
    }

    @Test
    public void deleteAsObservableDesc() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameDesc();

        rel.deleteAsMaybe(rel.get(2))
                .test()
                .assertResult(2);

        assertThat(rel.count(), is(2));
        assertThat(rel.get(0).name, is("C"));
        assertThat(rel.get(1).name, is("B"));
    }

    @Test
    public void testMultipleOrderingTerms() throws Exception {
        orma.createModelWithMultipleSortableColumns(new ModelFactory<ModelWithMultipleSortableColumns>() {
            @NonNull
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
            @NonNull
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
            @NonNull
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
    public void convertToContentValues() {
        ModelWithDate model = rel().get(0);
        ContentValues contentValues = rel().convertToContentValues(model, true);

        assertThat(contentValues.keySet(), containsInAnyOrder("name", "note", "time"));
        assertThat(contentValues.getAsString("name"), is(model.name));
        assertThat(contentValues.getAsString("note"), is(model.note));
        assertThat(contentValues.getAsLong("time"), is(BuiltInSerializers.serializeDate(model.time)));
    }

    @Test
    public void convertToArgs() {
        ModelWithDate model = rel().get(0);
        Object[] args = rel().convertToArgs(model, true);

        assertThat(Arrays.asList(args),
                containsInAnyOrder((Object) model.name, model.note, BuiltInSerializers.serializeDate(model.time)));
    }

    @Test
    public void iterable() throws Exception {
        RxRelation<ModelWithDate, ?> rel = rel().orderByNameAsc();

        int count = 0;
        for (ModelWithDate ModelWithDate : rel) {
            assertThat(ModelWithDate, is(notNullValue()));
            count++;
        }
        assertThat(count, is(3));

        count = 0;
        for (ModelWithDate ModelWithDate : rel) {
            assertThat(ModelWithDate, is(notNullValue()));
            count++;
        }
        assertThat(count, is(3));
    }

    @Test
    public void reload() throws Exception {
        ModelWithDate model = rel().get(0);
        rel().updater().nameEq(model.name).note("modified").execute();
        ModelWithDate reloaded = rel().reload(model);

        assertThat(reloaded.name, is(model.name));
        assertThat(reloaded.note, is("modified"));
    }

    @Test
    public void testClone() throws Exception {
        ModelWithDate_Relation rel = rel().orderByNameDesc().clone();

        assertThat(rel.get(0).name, is("C"));
        assertThat(rel.get(1).name, is("B"));
        assertThat(rel.get(2).name, is("A"));
    }
}
