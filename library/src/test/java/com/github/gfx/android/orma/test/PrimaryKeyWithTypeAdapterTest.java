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
import com.github.gfx.android.orma.test.model.ModelWithCustomPrimaryKey;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.EnumA;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.NonNull;
import androidx.test.runner.AndroidJUnit4;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PrimaryKeyWithTypeAdapterTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
    }

    @Test
    public void bigIntegerPrimaryKey() throws Exception {
        final ModelWithCustomPrimaryKey model = db
                .createModelWithCustomPrimaryKey(new ModelFactory<ModelWithCustomPrimaryKey>() {
                    @NonNull
                    @Override
                    public ModelWithCustomPrimaryKey call() {
                        return ModelWithCustomPrimaryKey.create(EnumA.FOO);
                    }
                });

        ModelWithCustomPrimaryKey.Holder holder = db.createHolder(new ModelFactory<ModelWithCustomPrimaryKey.Holder>() {
            @NonNull
            @Override
            public ModelWithCustomPrimaryKey.Holder call() {
                ModelWithCustomPrimaryKey.Holder holder = new ModelWithCustomPrimaryKey.Holder();
                holder.object = model;
                return holder;
            }
        });

        assertThat(holder.object.id, is(EnumA.FOO));

        assertThat(db.selectFromHolder().objectEq(EnumA.FOO).value().object.id, is(EnumA.FOO));
        assertThat(db.selectFromHolder().objectEq(model).value().object.id, is(EnumA.FOO));
    }
}
