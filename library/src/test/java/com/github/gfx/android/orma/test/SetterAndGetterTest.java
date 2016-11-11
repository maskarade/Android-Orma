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

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.test.model.ModelWithAccessors;
import com.github.gfx.android.orma.test.model.ModelWithNamedSetterConstructor;
import com.github.gfx.android.orma.test.model.ModelWithSetterConstructor;
import com.github.gfx.android.orma.test.model.ModelWithSetterConstructorAndNullable;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class SetterAndGetterTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
    }

    @Test
    public void testSetterAndGetter() throws Exception {
        ModelWithAccessors model = db.createModelWithAccessors(new ModelFactory<ModelWithAccessors>() {
            @NonNull
            @Override
            public ModelWithAccessors call() {
                ModelWithAccessors model = new ModelWithAccessors();
                model.setKey("key");
                model.setValue("get");
                return model;
            }
        });

        assertThat(model.getId(), is(not(0L)));
        assertThat(model.getKey(), is("key"));
        assertThat(model.getValue(), is("get"));
        assertThat(model.isDone(), is(nullValue()));
    }

    @Test
    public void testSetterConstructor() throws Exception {
        ModelWithSetterConstructor model = db.createModelWithSetterConstructor(new ModelFactory<ModelWithSetterConstructor>() {
            @NonNull
            @Override
            public ModelWithSetterConstructor call() {
                return new ModelWithSetterConstructor(0, "key", "get");
            }
        });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is("get"));
    }

    @Test
    public void testNamedSetterConstructor() throws Exception {
        ModelWithNamedSetterConstructor model = db.createModelWithNamedSetterConstructor(
                new ModelFactory<ModelWithNamedSetterConstructor>() {
                    @NonNull
                    @Override
                    public ModelWithNamedSetterConstructor call() {
                        return new ModelWithNamedSetterConstructor(0, "key", "get");
                    }
                });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is("get"));
    }

    @Test
    public void testSetterConstructorAndNullable0() throws Exception {
        ModelWithSetterConstructorAndNullable model = db
                .createModelWithSetterConstructorAndNullable(new ModelFactory<ModelWithSetterConstructorAndNullable>() {
                    @NonNull
                    @Override
                    public ModelWithSetterConstructorAndNullable call() {
                        return new ModelWithSetterConstructorAndNullable(0, "key", "get");
                    }
                });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is("get"));
    }

    @Test
    public void testSetterConstructorAndNullable1() throws Exception {
        ModelWithSetterConstructorAndNullable model = db
                .createModelWithSetterConstructorAndNullable(new ModelFactory<ModelWithSetterConstructorAndNullable>() {
                    @NonNull
                    @Override
                    public ModelWithSetterConstructorAndNullable call() {
                        return new ModelWithSetterConstructorAndNullable(0, "key", null);
                    }
                });

        assertThat(model.id, is(not(0L)));
        assertThat(model.key, is("key"));
        assertThat(model.value, is(nullValue()));
    }

}
