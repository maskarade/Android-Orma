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

import com.github.gfx.android.orma.internal.TypeHolder;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class TypeHolderTest {

    Type a = new TypeHolder<List<String>>() {
    }.getType();

    Type b = new TypeHolder<List<String>>() {
    }.getType();

    Type c = new TypeHolder<Collection<String>>() {
    }.getType();

    Type d = new TypeHolder<List<Integer>>() {
    }.getType();

    @Test
    public void testEquals() throws Exception {
        assertThat(a, is(b));
        assertThat(a, is(not(c)));
        assertThat(a, is(not(d)));
    }

    @Test
    public void testHashCode() throws Exception {
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void compatibiityWithParameterizedType() throws Exception {
        ParameterizedType t = (ParameterizedType) a;

        assertThat(t.getRawType(), is((Type) List.class));
        assertThat(t.getActualTypeArguments().length, is(1));
        assertThat(t.getActualTypeArguments()[0], is((Type) String.class));
    }
}
