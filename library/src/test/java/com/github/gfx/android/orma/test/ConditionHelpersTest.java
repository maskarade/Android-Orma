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
import com.github.gfx.android.orma.Inserter;
import com.github.gfx.android.orma.TransactionTask;
import com.github.gfx.android.orma.test.model.ModelWithConditionHelpers;
import com.github.gfx.android.orma.test.model.ModelWithConditionHelpers_Relation;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ConditionHelpersTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaDatabase.builder(getContext())
                .name(null)
                .build();

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
                    inserter.execute(model);
                }
            }
        });
    }

    ModelWithConditionHelpers_Relation rel() {
        return db.selectFromModelWithConditionHelpers();
    }

    @Test
    public void testIsNull() throws Exception {
        assertThat(rel().nullableTextIsNull().count(), is(0));
    }

    @Test
    public void testEq() throws Exception {
        assertThat(rel().nonNullTextEq("non-null text 1").count(), is(1));
        assertThat(rel().nullableTextEq("nullable text 1").count(), is(1));
        assertThat(rel().booleanValueEq(true).count(), is(1));
        assertThat(rel().byteValueEq((byte) 1).count(), is(1));
        assertThat(rel().shortValueEq((short) 1).count(), is(1));
        assertThat(rel().intValueEq(1).count(), is(1));
        assertThat(rel().longValueEq((long) 1).count(), is(1));
        assertThat(rel().floatValueEq((float) 1).count(), is(1));
        assertThat(rel().doubleValueEq((double) 1).count(), is(1));
    }

    @Test
    public void testNotEq() throws Exception {
        assertThat(rel().nonNullTextNotEq("non-null text 1").count(), is(9));
        assertThat(rel().nullableTextNotEq("nullable text 1").count(), is(9));
        assertThat(rel().booleanValueNotEq(true).count(), is(9));
        assertThat(rel().byteValueNotEq((byte) 1).count(), is(9));
        assertThat(rel().shortValueNotEq((short) 1).count(), is(9));
        assertThat(rel().intValueNotEq(1).count(), is(9));
        assertThat(rel().longValueNotEq((long) 1).count(), is(9));
        assertThat(rel().floatValueNotEq((float) 1).count(), is(9));
        assertThat(rel().doubleValueNotEq((double) 1).count(), is(9));
    }

    @Test
    public void testIn() throws Exception {
        assertThat(rel().nonNullTextIn(Arrays.asList("non-null text 1", "non-null text 4")).count(), is(2));
        assertThat(rel().nullableTextIn(Arrays.asList("nullable text 1", "nullable text 2")).count(), is(2));
        assertThat(rel().booleanValueIn(Collections.singleton(true)).count(), is(1));
        assertThat(rel().byteValueIn(Arrays.asList((byte) 1, (byte) 2)).count(), is(2));
        assertThat(rel().shortValueIn(Arrays.asList((short) 1, (short) 2)).count(), is(2));
        assertThat(rel().intValueIn(Arrays.asList(1, 2)).count(), is(2));
        assertThat(rel().longValueIn(Arrays.asList((long) 1, (long) 2)).count(), is(2));
        assertThat(rel().floatValueIn(Arrays.asList((float) 1, (float) 2)).count(), is(2));
        assertThat(rel().doubleValueIn(Arrays.asList((double) 1, (double) 2)).count(), is(2));
    }

    @Test
    public void testNotIn() throws Exception {
        assertThat(rel().nonNullTextNotIn(Arrays.asList("non-null text 1", "non-null text 2")).count(), is(8));
        assertThat(rel().nullableTextNotIn(Arrays.asList("nullable text 1", "nullable text 2")).count(), is(8));
        assertThat(rel().booleanValueNotIn(Collections.singleton(true)).count(), is(9));
        assertThat(rel().byteValueNotIn(Arrays.asList((byte) 1, (byte) 2)).count(), is(8));
        assertThat(rel().shortValueNotIn(Arrays.asList((short) 1, (short) 2)).count(), is(8));
        assertThat(rel().intValueNotIn(Arrays.asList(1, 2)).count(), is(8));
        assertThat(rel().longValueNotIn(Arrays.asList((long) 1, (long) 2)).count(), is(8));
        assertThat(rel().floatValueNotIn(Arrays.asList((float) 1, (float) 2)).count(), is(8));
        assertThat(rel().doubleValueNotIn(Arrays.asList((double) 1, (double) 2)).count(), is(8));
    }

    @Test
    public void testLt() throws Exception {
        assertThat(rel().byteValueLt((byte) 3).count(), is(3));
        assertThat(rel().shortValueLt((short) 3).count(), is(3));
        assertThat(rel().intValueLt(3).count(), is(3));
        assertThat(rel().longValueLt((long) 3).count(), is(3));
        assertThat(rel().floatValueLt((float) 3).count(), is(3));
        assertThat(rel().doubleValueLt((double) 3).count(), is(3));
    }

    @Test
    public void testLe() throws Exception {
        assertThat(rel().byteValueLe((byte) 3).count(), is(4));
        assertThat(rel().shortValueLe((short) 3).count(), is(4));
        assertThat(rel().intValueLe(3).count(), is(4));
        assertThat(rel().longValueLe((long) 3).count(), is(4));
        assertThat(rel().floatValueLe((float) 3).count(), is(4));
        assertThat(rel().doubleValueLe((double) 3).count(), is(4));
    }

    @Test
    public void testGt() throws Exception {
        assertThat(rel().byteValueGt((byte) 3).count(), is(6));
        assertThat(rel().shortValueGt((short) 3).count(), is(6));
        assertThat(rel().intValueGt(3).count(), is(6));
        assertThat(rel().longValueGt((long) 3).count(), is(6));
        assertThat(rel().floatValueGt((float) 3).count(), is(6));
        assertThat(rel().doubleValueGt((double) 3).count(), is(6));
    }

    @Test
    public void testGe() throws Exception {
        assertThat(rel().byteValueGe((byte) 3).count(), is(7));
        assertThat(rel().shortValueGe((short) 3).count(), is(7));
        assertThat(rel().intValueGe(3).count(), is(7));
        assertThat(rel().longValueGe((long) 3).count(), is(7));
        assertThat(rel().floatValueGe((float) 3).count(), is(7));
        assertThat(rel().doubleValueGe((double) 3).count(), is(7));
    }

    @Test
    public void testUpdater() throws Exception {
        db.updateModelWithConditionHelpers()
                .intValueEq(5)
                .longValue(100)
                .execute();

        assertThat(rel().intValueEq(5).value().longValue, is(100L));
    }

    @Test
    public void testDeleter() throws Exception {
        db.deleteFromModelWithConditionHelpers()
                .intValueEq(5)
                .execute();

        assertThat(rel().intValueEq(5).count(), is(0));
    }
}
