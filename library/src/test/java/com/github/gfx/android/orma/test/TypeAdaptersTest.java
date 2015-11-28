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

        String serialized = registry.serialize(stringListType, a);
        List<String> b = registry.deserialize(stringListType, serialized);

        assertThat(b, is(a));
    }

    @Test
    public void testStringSetAdapter() throws Exception {
        Set<String> a = new HashSet<>();
        a.add("foo");
        a.add("bar");

        String serialized = registry.serialize(stringSetType, a);
        Set<String> b = registry.deserialize(stringSetType, serialized);

        assertThat(b, is(a));
    }


    @Test
    public void testUriAdapter() throws Exception {
        Uri a = Uri.parse("http://example.com/foo?bar#baz");

        String serialized = registry.serialize(Uri.class, a);
        Uri b = registry.deserialize(Uri.class, serialized);

        assertThat(b, is(a));
    }

    @Test
    public void serializeNullable() throws Exception {
        assertThat(registry.serializeNullable(Uri.class, null), is(nullValue()));
    }

    @Test
    public void deserializeNullable() throws Exception {
        assertThat(registry.deserializeNullable(Uri.class, null), is(nullValue()));
    }

    @Test(expected = TypeAdapterNotFoundException.class)
    public void serializeNonRegisteredType() throws Exception {
        registry.serialize(TypeAdaptersTest.class, this);
    }

    @Test(expected = TypeAdapterNotFoundException.class)
    public void deserializeNonRegisteredType() throws Exception {
        registry.deserialize(TypeAdaptersTest.class, "");
    }

}
