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
import com.github.gfx.android.orma.test.model.ModelWithCompositeIndex;
import com.github.gfx.android.orma.test.model.ModelWithCompositeIndex_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.sqlite.SQLiteConstraintException;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class CompositeIndexTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        db.transactionSync(new Runnable() {
            @Override
            public void run() {
                Inserter<ModelWithCompositeIndex> inserter = db.prepareInsertIntoModelWithCompositeIndex();

                for (int i = 0; i < 5; i++) {
                    inserter.execute(new ModelWithCompositeIndex(
                            i / 2,
                            "c2:" + (i % 2 == 0 ? "even" : "odd"),

                            i / 2,
                            "c4:" + (i % 2 == 0 ? "even" : "odd")
                    ));
                }
            }
        });
    }

    @Test
    public void comositeIndexEq() throws Exception {
        ModelWithCompositeIndex_Selector s = db.selectFromModelWithCompositeIndex()
                .c1AndC2Eq(0, "c2:even");
        assertThat(s.count(), is(1));

        s = db.selectFromModelWithCompositeIndex()
                .c4AndC3Eq("c4:odd", 1);
        assertThat(s.count(), is(1));
    }

    @Test(expected = SQLiteConstraintException.class)
    public void compositeUniqueIndex() throws Exception {
        db.insertIntoModelWithCompositeIndex(
                new ModelWithCompositeIndex(1, "foo", 0, "c4:even")
        );
    }

    @Test
    public void orderByIndexedColumns() throws Exception {
        assertThat(db.selectFromModelWithCompositeIndex()
                .orderByc1AndC2Asc().value().c1, is(0L));
        assertThat(db.selectFromModelWithCompositeIndex()
                .orderByc1AndC2Desc().value().c1, is(2L));

        assertThat(db.selectFromModelWithCompositeIndex()
                .orderByc4AndC3Asc().value().c1, is(0L));
        assertThat(db.selectFromModelWithCompositeIndex()
                .orderByc4AndC3Desc().value().c1, is(1L));
    }
}
