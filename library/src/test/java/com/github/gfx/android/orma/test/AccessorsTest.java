package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ModelBuilder;
import com.github.gfx.android.orma.test.model.ModelWithAccessors;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class AccessorsTest {

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
    public void testAccessors() throws Exception {
        ModelWithAccessors model = db.createModelWithAccessors(new ModelBuilder<ModelWithAccessors>() {
            @Override
            public ModelWithAccessors build() {
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
}
