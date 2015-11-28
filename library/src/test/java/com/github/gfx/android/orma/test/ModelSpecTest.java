package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ModelBuilder;
import com.github.gfx.android.orma.test.model.ModelWithBlob;
import com.github.gfx.android.orma.test.model.ModelWithCollation;
import com.github.gfx.android.orma.test.model.ModelWithDefaults;
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
        ModelWithDefaults model = db.createModelWithDefaults(new ModelBuilder<ModelWithDefaults>() {
            @Override
            public ModelWithDefaults build() {
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
        ModelWithBlob model = db.createModelWithBlob(new ModelBuilder<ModelWithBlob>() {
            @Override
            public ModelWithBlob build() {
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
        ModelWithTypeAdapters model = db.createModelWithTypeAdapters(new ModelBuilder<ModelWithTypeAdapters>() {
            @NonNull
            @Override
            public ModelWithTypeAdapters build() {
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
}
