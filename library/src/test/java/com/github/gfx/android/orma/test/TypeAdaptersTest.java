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
import com.github.gfx.android.orma.adapter.StringListAdapter;
import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.exception.TypeAdapterNotFoundException;
import com.github.gfx.android.orma.internal.ParameterizedTypes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.net.Uri;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class TypeAdaptersTest {

    static final Type stringListType = ParameterizedTypes.getType(new ParameterizedTypes.TypeHolder<List<String>>() {
    });

    static final Type stringSetType = ParameterizedTypes.getType(new ParameterizedTypes.TypeHolder<Set<String>>() {
    });

    static final StringListAdapter stringListAdapter = new StringListAdapter();

    TypeAdapterRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new TypeAdapterRegistry();
        registry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
    }

    @Test
    public void testTypeHolder() throws Exception {
        assertThat(stringListType, is(stringListAdapter.getSourceType()));
    }

    @Test
    public void testStringListAdapter() throws Exception {
        List<String> a = Arrays.asList("foo", "bar");

        String serialized = registry.get(stringListType).serialize(a);
        List<String> b = registry.<List<String>>get(stringListType).deserialize(serialized);

        assertThat(b, is(a));
    }

    @Test
    public void testStringSetAdapter() throws Exception {
        Set<String> a = new HashSet<>();
        a.add("foo");
        a.add("bar");

        String serialized = registry.get(stringSetType).serialize(a);
        Set<String> b = registry.<Set<String>>get(stringSetType).deserialize(serialized);

        assertThat(b, is(a));
    }


    @Test
    public void testUriAdapter() throws Exception {
        Uri a = Uri.parse("http://example.com/foo?bar#baz");

        String serialized = registry.get(Uri.class).serialize(a);
        Uri b = registry.get(Uri.class).deserialize(serialized);

        assertThat(b, is(a));
    }

    @Test
    public void serializeNullable() throws Exception {
        assertThat(registry.get(Uri.class).serializeNullable(null), is(nullValue()));
    }

    @Test
    public void deserializeNullable() throws Exception {
        assertThat(registry.get(Uri.class).deserializeNullable(null), is(nullValue()));
    }

    @Test(expected = TypeAdapterNotFoundException.class)
    public void serializeNonRegisteredType() throws Exception {
        registry.get(TypeAdaptersTest.class);
    }
}
