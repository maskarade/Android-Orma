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
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.test.model.ModelWithConditionHelpers;
import com.github.gfx.android.orma.test.model.ModelWithConditionHelpers_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ConditionHelpersTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        db.transactionSync(new TransactionTask() {
            @Override
            public void execute() throws Exception {
                Inserter<ModelWithConditionHelpers> inserter = db.prepareInsertIntoModelWithConditionHelpers();

                for (int i = 0; i < 10; i++) {
                    ModelWithConditionHelpers model = new ModelWithConditionHelpers();
                    model.nullableText = "nullable text " + i;
                    model.nonNullText = "non-null text " + i;
                    model.booleanValue = i == 0;
                    model.byteValue = (byte) i;
                    model.shortValue = (short) i;
                    model.intValue = i;
                    model.longValue = (long) i;
                    model.floatValue = (float) i;
                    model.doubleValue = (double) i;
                    model.dateValue = new Date(i);
                    inserter.execute(model);
                }
            }
        });
    }

    ModelWithConditionHelpers_Selector selector() {
        return db.selectFromModelWithConditionHelpers();
    }

    @Test
    public void testIsNull() throws Exception {
        assertThat(selector().nullableTextIsNull().count(), is(0));
    }

    @Test
    public void testEq() throws Exception {
        assertThat(selector().idEq(1).count(), is(1));
        assertThat(selector().nonNullTextEq("non-null text 1").count(), is(1));
        assertThat(selector().nullableTextEq("nullable text 1").count(), is(1));
        assertThat(selector().booleanValueEq(true).count(), is(1));
        assertThat(selector().byteValueEq((byte) 1).count(), is(1));
        assertThat(selector().shortValueEq((short) 1).count(), is(1));
        assertThat(selector().intValueEq(1).count(), is(1));
        assertThat(selector().longValueEq((long) 1).count(), is(1));
        assertThat(selector().floatValueEq((float) 1).count(), is(1));
        assertThat(selector().doubleValueEq((double) 1).count(), is(1));
        assertThat(selector().dateValueEq(new Date(1)).count(), is(1));
    }

    @Test
    public void testNotEq() throws Exception {
        assertThat(selector().nonNullTextNotEq("non-null text 1").count(), is(9));
        assertThat(selector().nullableTextNotEq("nullable text 1").count(), is(9));
        assertThat(selector().booleanValueNotEq(true).count(), is(9));
        assertThat(selector().byteValueNotEq((byte) 1).count(), is(9));
        assertThat(selector().shortValueNotEq((short) 1).count(), is(9));
        assertThat(selector().intValueNotEq(1).count(), is(9));
        assertThat(selector().longValueNotEq((long) 1).count(), is(9));
        assertThat(selector().floatValueNotEq((float) 1).count(), is(9));
        assertThat(selector().doubleValueNotEq((double) 1).count(), is(9));
        assertThat(selector().dateValueNotEq(new Date(1)).count(), is(9));
    }

    @Test
    public void testIn() throws Exception {
        assertThat(selector().idIn(Arrays.asList(1L, 2L)).count(), is(2));
        assertThat(selector().nonNullTextIn(Arrays.asList("non-null text 1", "non-null text 4")).count(), is(2));
        assertThat(selector().nullableTextIn(Arrays.asList("nullable text 1", "nullable text 2")).count(), is(2));
        assertThat(selector().booleanValueIn(Collections.singleton(true)).count(), is(1));
        assertThat(selector().byteValueIn(Arrays.asList((byte) 1, (byte) 2)).count(), is(2));
        assertThat(selector().shortValueIn(Arrays.asList((short) 1, (short) 2)).count(), is(2));
        assertThat(selector().intValueIn(Arrays.asList(1, 2)).count(), is(2));
        assertThat(selector().longValueIn(Arrays.asList((long) 1, (long) 2)).count(), is(2));
        assertThat(selector().floatValueIn(Arrays.asList((float) 1, (float) 2)).count(), is(2));
        assertThat(selector().doubleValueIn(Arrays.asList((double) 1, (double) 2)).count(), is(2));
        assertThat(selector().dateValueIn(Arrays.asList(new Date(1), new Date(2))).count(), is(2));

        assertThat(selector().dateValueIn(new Date(1), new Date(2)).count(), is(2));
    }

    @Test
    public void testNotIn() throws Exception {
        assertThat(selector().nonNullTextNotIn(Arrays.asList("non-null text 1", "non-null text 2")).count(), is(8));
        assertThat(selector().nullableTextNotIn(Arrays.asList("nullable text 1", "nullable text 2")).count(), is(8));
        assertThat(selector().booleanValueNotIn(Collections.singleton(true)).count(), is(9));
        assertThat(selector().byteValueNotIn(Arrays.asList((byte) 1, (byte) 2)).count(), is(8));
        assertThat(selector().shortValueNotIn(Arrays.asList((short) 1, (short) 2)).count(), is(8));
        assertThat(selector().intValueNotIn(Arrays.asList(1, 2)).count(), is(8));
        assertThat(selector().longValueNotIn(Arrays.asList((long) 1, (long) 2)).count(), is(8));
        assertThat(selector().floatValueNotIn(Arrays.asList((float) 1, (float) 2)).count(), is(8));
        assertThat(selector().doubleValueNotIn(Arrays.asList((double) 1, (double) 2)).count(), is(8));
        assertThat(selector().dateValueNotIn(Arrays.asList(new Date(1), new Date(2))).count(), is(8));

        assertThat(selector().dateValueNotIn(new Date(1), new Date(2)).count(), is(8));
    }

    @Test
    public void testLt() throws Exception {
        assertThat(selector().byteValueLt((byte) 3).count(), is(3));
        assertThat(selector().shortValueLt((short) 3).count(), is(3));
        assertThat(selector().intValueLt(3).count(), is(3));
        assertThat(selector().longValueLt((long) 3).count(), is(3));
        assertThat(selector().floatValueLt((float) 3).count(), is(3));
        assertThat(selector().doubleValueLt((double) 3).count(), is(3));
        assertThat(selector().dateValueLt(new Date(3)).count(), is(3));
    }

    @Test
    public void testLe() throws Exception {
        assertThat(selector().byteValueLe((byte) 3).count(), is(4));
        assertThat(selector().shortValueLe((short) 3).count(), is(4));
        assertThat(selector().intValueLe(3).count(), is(4));
        assertThat(selector().longValueLe((long) 3).count(), is(4));
        assertThat(selector().floatValueLe((float) 3).count(), is(4));
        assertThat(selector().doubleValueLe((double) 3).count(), is(4));
        assertThat(selector().dateValueLe(new Date(3)).count(), is(4));
    }

    @Test
    public void testGt() throws Exception {
        assertThat(selector().byteValueGt((byte) 3).count(), is(6));
        assertThat(selector().shortValueGt((short) 3).count(), is(6));
        assertThat(selector().intValueGt(3).count(), is(6));
        assertThat(selector().longValueGt((long) 3).count(), is(6));
        assertThat(selector().floatValueGt((float) 3).count(), is(6));
        assertThat(selector().doubleValueGt((double) 3).count(), is(6));
        assertThat(selector().dateValueGt(new Date(3)).count(), is(6));
    }

    @Test
    public void testGe() throws Exception {
        assertThat(selector().byteValueGe((byte) 3).count(), is(7));
        assertThat(selector().shortValueGe((short) 3).count(), is(7));
        assertThat(selector().intValueGe(3).count(), is(7));
        assertThat(selector().longValueGe((long) 3).count(), is(7));
        assertThat(selector().floatValueGe((float) 3).count(), is(7));
        assertThat(selector().doubleValueGe((double) 3).count(), is(7));
        assertThat(selector().dateValueGe(new Date(3)).count(), is(7));
    }

    @Test
    public void testUpdater() throws Exception {
        db.updateModelWithConditionHelpers()
                .intValueEq(5)
                .longValue(100)
                .execute();

        assertThat(selector().intValueEq(5).value().longValue, is(100L));
    }

    @Test
    public void testDeleter() throws Exception {
        db.deleteFromModelWithConditionHelpers()
                .intValueEq(5)
                .execute();

        assertThat(selector().intValueEq(5).count(), is(0));
    }
}
