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

import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Where;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ReservedWordsTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
    }

    @Test
    public void useReservedWordsInInsert() throws Exception {
        Where where = new Where();
        where.where = "a";
        where.table = "b";
        where.on = "c";
        long rowId = db.insertIntoWhere(where);
        assertThat(rowId, is(1L));
    }

    @Test
    public void useReservedWordsInSelect() throws Exception {
        useReservedWordsInInsert();

        assertThat(db.selectFromWhere().toList(), hasSize(1));
    }

    @Test
    public void useReservedWordsInConditionQueryHelpers() throws Exception {
        useReservedWordsInInsert();

        assertThat(db.selectFromWhere()
                        .whereEq("a")
                        .tableEq("b")
                        .onEq("c")
                        .toList(),
                hasSize(1));
    }

    @Test
    public void useReservedWordsInUpdate() throws Exception {
        useReservedWordsInInsert();

        assertThat(db.updateWhere()
                        .where("foo")
                        .table("bar")
                        .on("baz")
                        .execute(),
                is(1));
    }

    @Test
    public void useReservedWordsInDelete() throws Exception {
        useReservedWordsInInsert();

        assertThat(db.deleteFromWhere()
                        .execute(),
                is(1));
    }
}
