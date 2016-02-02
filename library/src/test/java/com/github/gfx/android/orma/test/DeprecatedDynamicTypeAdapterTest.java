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
import com.github.gfx.android.orma.adapter.AbstractTypeAdapter;
import com.github.gfx.android.orma.test.model.ModelWithDeprecatedTypeAdapter;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.IntTuple2x;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Will be removed in v2.0
 */
@RunWith(AndroidJUnit4.class)
public class DeprecatedDynamicTypeAdapterTest {

    @SuppressWarnings("deprecated")
    @Test
    public void testDynamicTypeAdapter() throws Exception {
        OrmaDatabase orma = OrmaDatabase.builder(InstrumentationRegistry.getTargetContext())
                .typeAdapters(new AbstractTypeAdapter<IntTuple2x>() {
                    @NonNull
                    @Override
                    public String serialize(@NonNull IntTuple2x tuple) {
                        return String.valueOf(((long) tuple.first << 32) | (long) tuple.second);
                    }

                    @NonNull
                    @Override
                    public IntTuple2x deserialize(@NonNull String serialized) {
                        long value = Long.parseLong(serialized);
                        return new IntTuple2x((int) (value >> 32), (int) value);
                    }
                })
                .build();

       ModelWithDeprecatedTypeAdapter model = orma.createModelWithDeprecatedTypeAdapter(
                new ModelFactory<ModelWithDeprecatedTypeAdapter>() {
                    @Override
                    public ModelWithDeprecatedTypeAdapter call() {
                        ModelWithDeprecatedTypeAdapter model = new ModelWithDeprecatedTypeAdapter();
                        model.intTuple2 = new IntTuple2x(-13, 17);
                        return model;
                    }
                });

        assertThat(model.intTuple2, is(new IntTuple2x(-13, 17)));
    }
}
