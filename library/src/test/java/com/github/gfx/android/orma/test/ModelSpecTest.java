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
import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.test.model.ModelWithBlob;
import com.github.gfx.android.orma.test.model.ModelWithBoxTypes;
import com.github.gfx.android.orma.test.model.ModelWithCollation;
import com.github.gfx.android.orma.test.model.ModelWithDefaults;
import com.github.gfx.android.orma.test.model.ModelWithPrimitives;
import com.github.gfx.android.orma.test.model.ModelWithTypeAdapters;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ModelSpecTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaDatabase.builder(getContext())
                .name(null)
                .build();
    }

    @Test
    public void testDefaultValue() throws Exception {
        ModelWithDefaults model = db.createModelWithDefaults(new ModelFactory<ModelWithDefaults>() {
            @NonNull
            @Override
            public ModelWithDefaults create() {
                return new ModelWithDefaults();
            }
        });

        assertThat(model.s, is("foo"));
        assertThat(model.i, is(10L));
    }

    @Test
    public void testCollation() throws Exception {
        ModelWithCollation one = new ModelWithCollation();
        one.noCollationField = "foo";
        one.rtrimField = "foo";
        one.nocaseField = "foo";
        db.insertIntoModelWithCollation(one);

        ModelWithCollation two = new ModelWithCollation();
        two.noCollationField = "foo  ";
        two.rtrimField = "foo  ";
        two.nocaseField = "foo  ";
        db.insertIntoModelWithCollation(two);

        ModelWithCollation three = new ModelWithCollation();
        three.noCollationField = "FOO";
        three.rtrimField = "FOO";
        three.nocaseField = "FOO";
        db.insertIntoModelWithCollation(three);

        assertThat(db.selectFromModelWithCollation().where("rtrimField = ?", "foo ").count(), is(2));
        assertThat(db.selectFromModelWithCollation().where("nocaseField = ?", "foo").count(), is(2));
        assertThat(db.selectFromModelWithCollation().where("noCollationField = ?", "foo").count(), is(1));
    }

    @Test
    public void testBlob() throws Exception {
        ModelWithBlob model = db.createModelWithBlob(new ModelFactory<ModelWithBlob>() {
            @NonNull
            @Override
            public ModelWithBlob create() {
                ModelWithBlob model = new ModelWithBlob();
                model.blob = new byte[]{0, 1, 2, 3};
                return model;
            }
        });

        assertThat(model.blob, is(new byte[]{0, 1, 2, 3}));
    }

    @Test
    public void testObjectMapping() throws Exception {
        final long now = new Date().getTime();

        System.out.println(db.getConnection().getTypeAdapterRegistry().toString());
        ModelWithTypeAdapters model = db.createModelWithTypeAdapters(new ModelFactory<ModelWithTypeAdapters>() {
            @NonNull
            @Override
            public ModelWithTypeAdapters create() {
                ModelWithTypeAdapters model = new ModelWithTypeAdapters();
                model.list = Arrays.asList("foo", "bar", "baz");
                model.set = new HashSet<>();
                model.set.add("foo");
                model.set.add("bar");
                model.set.add("baz");
                model.uri = Uri.parse("http://example.com");
                model.date = new Date(now);
                return model;
            }
        });

        assertThat(model.list, contains("foo", "bar", "baz"));
        assertThat(model.set, containsInAnyOrder("foo", "bar", "baz"));
        assertThat(model.uri, is(Uri.parse("http://example.com")));
        assertThat(model.date, is(new Date(now)));
    }

    @Test
    public void testPrimitives() throws Exception {
        ModelWithPrimitives model = db.createModelWithPrimitives(new ModelFactory<ModelWithPrimitives>() {
            @NonNull
            @Override
            public ModelWithPrimitives create() {
                ModelWithPrimitives model = new ModelWithPrimitives();

                model.booleanValue = true;
                model.byteValue = 1;
                model.shortValue = 2;
                model.intValue = 3;
                model.longValue = 4L;
                model.floatValue = 1.14f;
                model.doubleValue = 3.14;

                return model;
            }
        });

        assertThat(model.booleanValue, is(true));
        assertThat(model.byteValue, is((byte) 1));
        assertThat(model.shortValue, is((short) 2));
        assertThat(model.intValue, is(3));
        assertThat(model.longValue, is(4L));
        assertThat(model.floatValue, is(1.14f));
        assertThat(model.doubleValue, is(3.14));
    }

    @Test
    public void testBoxTypes() throws Exception {
        ModelWithBoxTypes model = db.createModelWithBoxTypes(new ModelFactory<ModelWithBoxTypes>() {
            @NonNull
            @Override
            public ModelWithBoxTypes create() {
                ModelWithBoxTypes model = new ModelWithBoxTypes();

                model.booleanValue = true;
                model.byteValue = 1;
                model.shortValue = 2;
                model.intValue = 3;
                model.longValue = 4L;
                model.floatValue = 1.14f;
                model.doubleValue = 3.14;

                return model;
            }
        });

        assertThat(model.booleanValue, is(true));
        assertThat(model.byteValue, is((byte) 1));
        assertThat(model.shortValue, is((short) 2));
        assertThat(model.intValue, is(3));
        assertThat(model.longValue, is(4L));
        assertThat(model.floatValue, is(1.14f));
        assertThat(model.doubleValue, is(3.14));
    }

}
