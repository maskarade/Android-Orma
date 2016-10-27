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

import com.github.gfx.android.orma.test.model.ModelWithPrimitives;
import com.github.gfx.android.orma.test.model.ModelWithPrimitives_Schema;
import com.github.gfx.android.orma.test.model.ModelWithPrimitives_Selector;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class AggregatorTest {

    OrmaDatabase db;

    ModelWithPrimitives_Schema schema;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();

        schema = ModelWithPrimitives_Schema.INSTANCE;

        db.insertIntoModelWithPrimitives(ModelWithPrimitives.create(
                true,
                (byte) 10,
                (short) 10,
                10,
                10,
                10.0f,
                10.0
        ));

        db.insertIntoModelWithPrimitives(ModelWithPrimitives.create(
                true,
                (byte) 3,
                (short) 3,
                3,
                3,
                3.0f,
                3.0
        ));
    }

    ModelWithPrimitives_Selector selector() {
        return db.selectFromModelWithPrimitives();
    }

    @Test
    public void minBy() throws Exception {
        assertThat(selector().minByByteValue(), is((byte) 3));
        assertThat(selector().minByIntValue(), is(3));
        assertThat(selector().minByLongValue(), is(3L));
        assertThat(selector().minByFloatValue(), is(3.0f));
        assertThat(selector().minByDoubleValue(), is(3.0));
    }

    @Test
    public void maxBy() throws Exception {
        assertThat(selector().maxByByteValue(), is((byte) 10));
        assertThat(selector().maxByIntValue(), is(10));
        assertThat(selector().maxByLongValue(), is(10L));
        assertThat(selector().maxByFloatValue(), is(10.0f));
        assertThat(selector().maxByDoubleValue(), is(10.0));
    }

    @Test
    public void sumBy() throws Exception {
        assertThat(selector().sumByByteValue(), is(13L));
        assertThat(selector().sumByShortValue(), is(13L));
        assertThat(selector().sumByIntValue(), is(13L));
        assertThat(selector().sumByLongValue(), is(13L));
        assertThat(selector().sumByFloatValue(), is(13.0));
        assertThat(selector().sumByDoubleValue(), is(13.0));
    }

    @Test
    public void avgBy() throws Exception {
        assertThat(selector().avgByIntValue(), is(6.5));
        assertThat(selector().avgByDoubleValue(), is(6.5));
    }

    @Test
    public void nulls() throws Exception {
        db.deleteFromModelWithPrimitives().execute();

        assertThat(selector().minByIntValue(), is(nullValue()));
        assertThat(selector().maxByIntValue(), is(nullValue()));
        assertThat(selector().sumByIntValue(), is(nullValue()));
        assertThat(selector().avgByIntValue(), is(nullValue()));
    }
}
