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
import com.github.gfx.android.orma.test.model.ModelWithAccessors;
import com.github.gfx.android.orma.test.model.ModelWithNamedSetterConstructor;
import com.github.gfx.android.orma.test.model.ModelWithSetterConstructor;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.support.annotation.NonNull;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class SetterAndGetterTest {

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
    public void testSetterAndGetter() throws Exception {
        ModelWithAccessors model = db.createModelWithAccessors(new ModelFactory<ModelWithAccessors>() {
            @NonNull
            @Override
            public ModelWithAccessors call() {
                ModelWithAccessors model = new ModelWithAccessors();
                model.setKey("key");
                model.setValue("value");
                return model;
            }
        });

        assertThat(model.getId(), is(not(0L)));
        assertThat(model.getKey(), is("key"));
        assertThat(model.getValue(), is("value"));
    }

    @Test
    public void testSetterConstructor() throws Exception {
        ModelWithSetterConstructor model = db.createModelWithSetterConstructor(new ModelFactory<ModelWithSetterConstructor>() {
            @NonNull
            @Override
            public ModelWithSetterConstructor call() {
                return new ModelWithSetterConstructor(0, "key", "value");
            }
        });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is("value"));
    }

    @Test
    public void testNamedSetterConstructor() throws Exception {
        ModelWithNamedSetterConstructor model = db.createModelWithNamedSetterConstructor(
                new ModelFactory<ModelWithNamedSetterConstructor>() {
                    @NonNull
                    @Override
                    public ModelWithNamedSetterConstructor call() {
                        return new ModelWithNamedSetterConstructor(0, "key", "value");
                    }
                });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is("value"));
    }

}
